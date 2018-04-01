package com.ronis.executor.services;

import java.io.IOException;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.ronis.publisher.executor.commons.model.PublishParameters;
import com.ronis.publisher.executor.commons.services.Executor;

@Service("executorService")
public class ExecutorImpl implements Executor {

    private static final String HAZLECAST_SERVER_CONFIG_FILE = "hazelcast-server-executor.xml";

    private HazelcastInstance instance;

    @PostConstruct
    public void init() throws Exception {
        LOGGER.debug("Init Hazlecast server");
        instance = Hazelcast.newHazelcastInstance(loadHazelCastProperties());
    }

    private static Config loadHazelCastProperties() throws Exception {
        Config hazelcastConfig = null;
        try {
            hazelcastConfig = new FileSystemXmlConfig(
                    "C:\\workspace\\executor\\src\\main\\resources\\" + HAZLECAST_SERVER_CONFIG_FILE);
            LOGGER.info(
                    "Hazelcast client configuration file loaded succesefully from path");
        } catch (IllegalArgumentException | IOException e) {
            LOGGER.error("Error hazelcast server loading configuration {}", e);
            throw new Exception("hazelcast server configuration is not found, cannot start publisher application");
        }
        return hazelcastConfig;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorImpl.class);

    public String execute(PublishParameters publishParameters) {
        LOGGER.info("Recieved task with publish parameters: {}", publishParameters);
        publishParameters.setExecuteTimestamp(new Date(publishParameters.getExecuteTimestamp().getTime()));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Executor has been succesfully recieved task name: ")
                .append(publishParameters.getTaskName()).append(" from publisher: ")
                .append(publishParameters.getPublisherName()).append(" created on: ")
                .append(publishParameters.getPublishTimestamp()).append(" and executed on ")
                .append(publishParameters.getExecuteTimestamp());
        publishParameters.setExecutorResponse(stringBuilder.toString());
        LOGGER.info("Executed task with publish parameters: {}", publishParameters);
        return publishParameters.getExecutorResponse();
    }

    @PreDestroy
    public void destroy() {
        LOGGER.debug("destroy Hazlecast server");
        if (instance != null)
            instance.shutdown();
    }

}
