package org.motechproject.scheduletracking.api.service.impl;

import org.joda.time.LocalDate;
import org.motechproject.model.Time;
import org.motechproject.scheduletracking.api.domain.Enrollment;
import org.motechproject.scheduletracking.api.domain.EnrollmentStatus;
import org.motechproject.scheduletracking.api.domain.Schedule;
import org.motechproject.scheduletracking.api.domain.exception.NoMoreMilestonesToFulfillException;
import org.motechproject.scheduletracking.api.repository.AllEnrollments;
import org.motechproject.scheduletracking.api.repository.AllTrackedSchedules;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.motechproject.util.DateUtil.today;

@Component
public class EnrollmentService {
    private AllTrackedSchedules allTrackedSchedules;
    private AllEnrollments allEnrollments;
    private EnrollmentAlertService enrollmentAlertService;
    private EnrollmentDefaultmentService enrollmentDefaultmentService;

    @Autowired
    public EnrollmentService(AllTrackedSchedules allTrackedSchedules, AllEnrollments allEnrollments, EnrollmentAlertService enrollmentAlertService, EnrollmentDefaultmentService enrollmentDefaultmentService) {
        this.allTrackedSchedules = allTrackedSchedules;
        this.allEnrollments = allEnrollments;
        this.enrollmentAlertService = enrollmentAlertService;
        this.enrollmentDefaultmentService = enrollmentDefaultmentService;
    }

    public String enroll(String externalId, String scheduleName, String startingMilestoneName, LocalDate referenceDate, LocalDate enrollmentDate, Time preferredAlertTime) {
        Schedule schedule = allTrackedSchedules.getByName(scheduleName);
        EnrollmentStatus enrollmentStatus = EnrollmentStatus.Active;
        if(hasEnrollmentAlreadyExpired(referenceDate, schedule))
            enrollmentStatus = EnrollmentStatus.Defaulted;

        Enrollment enrollment = allEnrollments.addOrReplace(new Enrollment(externalId, scheduleName, startingMilestoneName, referenceDate, enrollmentDate, preferredAlertTime, enrollmentStatus));
        enrollmentAlertService.scheduleAlertsForCurrentMilestone(enrollment);
        enrollmentDefaultmentService.scheduleJobToCaptureDefaultment(enrollment);

        return enrollment.getId();
    }

    public void fulfillCurrentMilestone(Enrollment enrollment, LocalDate fulfillmentDate) {
        Schedule schedule = allTrackedSchedules.getByName(enrollment.getScheduleName());
        if (schedule.maxMilestoneCountReached(enrollment.getFulfillments().size()))
            throw new NoMoreMilestonesToFulfillException();

        enrollmentAlertService.unscheduleAllAlerts(enrollment);
        enrollmentDefaultmentService.unscheduleDefaultmentCaptureJob(enrollment);

        enrollment.fulfillCurrentMilestone(fulfillmentDate);
        String nextMilestoneName = schedule.getNextMilestoneName(enrollment.getCurrentMilestoneName());
        enrollment.setCurrentMilestoneName(nextMilestoneName);
        if (nextMilestoneName == null)
            enrollment.setStatus(EnrollmentStatus.Completed);
        else {
            enrollmentAlertService.scheduleAlertsForCurrentMilestone(enrollment);
            enrollmentDefaultmentService.scheduleJobToCaptureDefaultment(enrollment);
        }
        allEnrollments.update(enrollment);
    }

    public void unenroll(Enrollment enrollment) {
        enrollmentAlertService.unscheduleAllAlerts(enrollment);
        enrollmentDefaultmentService.unscheduleDefaultmentCaptureJob(enrollment);
        enrollment.setStatus(EnrollmentStatus.Unenrolled);
        allEnrollments.update(enrollment);
    }

    private boolean hasEnrollmentAlreadyExpired(LocalDate referenceDate, Schedule schedule) {
        return referenceDate.plus(schedule.getDuration()).isBefore(today());
    }
}
