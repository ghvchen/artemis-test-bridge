/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.artemis.amqp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.amqp.queue.ArtemisQueueReceiver;

public class ArtemisQueueReceiverDerived extends ArtemisQueueReceiver {
	final static Logger logger = LoggerFactory.getLogger(ArtemisQueueReceiverDerived.class);

	public ArtemisQueueReceiverDerived() throws Exception {
		super();
	}

	@Override
	public String executeRequestedTask(String command) {
		String output = "";

		// run the command and collect result from linux commands
		try {
			Process p = Runtime.getRuntime().exec(command);

			p.waitFor();
			BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";

			while ((line = buf.readLine()) != null) {
				output += line + "\n";
			}
		} catch (IOException ioex) {
			logger.error("Error executing command, IOException: " + ioex.getMessage());
		} catch (Exception ex) {
			logger.error("Error executing command: " + ex.getMessage());
		}

		return output;
	}
}
