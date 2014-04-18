/**
 * Copyright (C) FuseSource, Inc.
 * http://fusesource.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.insight.log.log4j;

import org.apache.log4j.spi.LoggingEvent;
import io.fabric8.insight.log.support.LogQuerySupportMBean;

/**
 * The MBean operations for {@link Log4jLogQuery}
 */
public interface Log4jLogQueryMBean extends LogQuerySupportMBean {

    /**
     * Provides a hook you can call if the underlying log4j
     * configuration is reloaded so that you can force the appender
     * to get re-registered.
     */
    void reconnectAppender();

    public void logMessage(LoggingEvent record);
}