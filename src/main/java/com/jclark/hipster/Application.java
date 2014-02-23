package com.jclark.hipster;

import com.jclark.hipster.config.Constants;
import com.jclark.hipster.config.reload.JHipsterPluginManagerReloadPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.MetricFilterAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.MetricRepositoryAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springsource.loaded.agent.SpringLoadedAgent;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;

@ComponentScan
@EnableAutoConfiguration(exclude = {MetricFilterAutoConfiguration.class, MetricRepositoryAutoConfiguration.class})
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Inject
    private Environment env;

    /**
     * Initializes hipste.
     * <p/>
     * Spring profiles can be configured with a program arguments --spring.profiles.active=your-active-profile
     * <p/>
     */
    @PostConstruct
    public void initApplication() throws IOException {
        if (env.getActiveProfiles().length == 0) {
            log.warn("No Spring profile configured, running with default configuration");
        } else {
            log.info("Running with Spring profile(s) : {}", env.getActiveProfiles());
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setShowBanner(false);

        // Fallback to set the list of liquibase package list
        addLiquibaseScanPackages();

        ConfigurableApplicationContext applicationContext = app.run(args);
        try {
            SpringLoadedAgent.getInstrumentation();
            log.info("Spring Loaded is running, registering hot reloading features");
            JHipsterPluginManagerReloadPlugin.register(applicationContext);
        } catch (UnsupportedOperationException uoe) {
            log.info("Spring Loaded is not running, hot reloading is not enabled");
        }
    }

    /**
     * Set the liquibases.scan.packages to avoid an exception from ServiceLocator
     * <p/>
     * See the following JIRA issue https://liquibase.jira.com/browse/CORE-677
     */
    private static void addLiquibaseScanPackages() {
        System.setProperty("liquibase.scan.packages", "liquibase.change" + "," + "liquibase.database" + "," +
                "liquibase.parser" + "," + "liquibase.precondition" + "," + "liquibase.datatype" + "," +
                "liquibase.serializer" + "," + "liquibase.sqlgenerator" + "," + "liquibase.executor" + "," +
                "liquibase.snapshot" + "," + "liquibase.logging" + "," + "liquibase.diff" + "," +
                "liquibase.structure" + "," + "liquibase.structurecompare" + "," + "liquibase.lockservice" + "," +
                "liquibase.ext" + "," + "liquibase.changelog");
    }
}
