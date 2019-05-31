package game.quartz.scheduler;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import game.quartz.job.clientsArrive;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;


public class clientsArriveScheduler {

    public static void main(String[] args) throws InterruptedException {
        start();
    }

    public static void start()
    {
        try {
            // Grab the Scheduler instance from the Factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            // define the job and tie it to our SimpleJob class
            JobDetail job = newJob(clientsArrive.class)
                    .withIdentity("job1", "group1")
                    .build();

            // Trigger the job to run now, and then repeat every 1 seconds
            Trigger trigger = newTrigger()
                    .withIdentity("trigger1", "group1")
                    .startNow()
                    .withSchedule(cronSchedule("0/5 * * * * ?"))
                    .build();


            // Tell quartz to schedule the job using our trigger
            scheduler.scheduleJob(job, trigger);

            // and start it off
            scheduler.start();

        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }
    }
