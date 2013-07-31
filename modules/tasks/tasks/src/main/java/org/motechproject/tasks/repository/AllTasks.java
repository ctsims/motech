package org.motechproject.tasks.repository;

import org.ektorp.CouchDbConnector;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.support.View;
import org.motechproject.commons.couchdb.dao.MotechBaseRepository;
import org.motechproject.tasks.domain.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AllTasks extends MotechBaseRepository<Task> {

    @Autowired
    public AllTasks(@Qualifier("taskDbConnector") final CouchDbConnector connector) {
        super(Task.class, connector);
    }

    public void addOrUpdate(Task task) {
        boolean exists = null != task.getId();
        Task existing = null;

        if (exists) {
            // When a user imports a task it contains '_id' property
            // but in database a task with this id may not exist
            // that's why we have to check if the task really exists in db
            try {
                existing = get(task.getId());
            } catch (DocumentNotFoundException ex) {
                exists = false;
            }
        }

        if (exists) {
            existing.setActions(task.getActions());
            existing.setDescription(task.getDescription());
            existing.setEnabled(task.isEnabled());
            existing.setTaskConfig(task.getTaskConfig());
            existing.setTrigger(task.getTrigger());
            existing.setName(task.getName());
            existing.setValidationErrors(task.getValidationErrors());

            update(existing);
        } else {
            add(task);
        }
    }

    @View(
            name = "by_triggerSubject",
            map = "function(doc) { if(doc.type === 'Task') emit(doc.trigger.subject); }"
    )
    public List<Task> byTriggerSubject(final String subject) {
        return queryView("by_triggerSubject", subject);
    }

}
