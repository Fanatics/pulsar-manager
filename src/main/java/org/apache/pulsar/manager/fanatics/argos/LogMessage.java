/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pulsar.manager.fanatics.argos;
public class LogMessage {

    public String an = "pulsar-manager";
    public String at = "java";
    public long ts ;
    public String lv;
    public String lm;

    public String si;

    public String rc;

    public String user;

    public LogMessage(String lv, String lm, String username) {
        this.lv = lv;
        this.lm = lm;
        ts = System.currentTimeMillis();
        si = "2a4046e3-af81-4584-b043-fefc5abdd8c4";
        rc = "481b9ddfbfdec29c";
        user = username;
    }
}
