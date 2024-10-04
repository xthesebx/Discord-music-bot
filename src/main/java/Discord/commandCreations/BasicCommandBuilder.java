package Discord.commandCreations;

import Discord.NewMain;
import com.hawolt.logger.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

/**
 * <p>BasicCommandBuilder class.</p>
 *
 * TODO: Rework to correclty being able to create commands with this (maybe with builder)
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class BasicCommandBuilder extends JSONObject {

    /**
     * <p>Constructor setting Default values</p>
     */
    public BasicCommandBuilder() {
        this.put("type", 1);
        this.put("contexts", new Integer[0]);
    }

    /**
     * <p>setName.</p>
     *
     * @param name a {@link java.lang.String} object
     */
    public void setName(String name) {
        this.put("name", name);
    }

    /**
     * <p>setDescription.</p>
     *
     * @param description a {@link java.lang.String} object
     */
    public void setDescription(String description) {
        this.put("description", description);
    }

    /**
     * <p>setType.</p>
     *
     * @param type a int
     */
    public void setType(int type) {
        this.put("type", type);
    }

    /**
     * <p>setDescriptionLocalization.</p>
     *
     * @param language a {@link java.lang.String} object
     * @param description a {@link java.lang.String} object
     */
    public void setDescriptionLocalization (String language, String description) {
        if (!this.has("description_localizations"))
            this.put("description_localizations", new JSONObject().put(language, description));
        else {
            this.getJSONObject("description_localizations").put(language, description);
        }
    }

    /**
     * <p>setContext</p>
     * @param context Contexts to set
     */

    public void setContext(Integer[] context) {
        this.put("contexts", context);
    }

    /**
     * <p>creates the Command</p>
     * @throws IOException
     */
    public void create() throws IOException {
        HttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://discord.com/api/v10/applications/1178647694923792404/commands");
        httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
        httpPost.setHeader("Authorization", "Bot " + NewMain.apikey);
        httpPost.setEntity(new StringEntity(this.toString()));
        HttpResponse response = client.execute(httpPost);
        if (response.getStatusLine().getStatusCode() != 201) {
            return;
        } else {
            Logger.error("error, status code is " + response.getStatusLine().getStatusCode());
        }

    }

    public JSONObject build() {
        return this;
    }

    public static class Option extends JSONObject {

        public void setName(String name) {
            this.put("name", name);
        }

        public void setDescription(String description) {
            this.put("description", description);
        }

        public void setType(int type) {
            this.put("type", type);
        }

        public void setRequired (boolean required) {
            this.put("required", required);
        }

        public void addChoice (String name, String value) {
            if(!this.has("choices")) {
                this.put("choices", new JSONArray());
            }
            this.getJSONArray("choices").put(new JSONObject().put("name", name).put("value", value));
        }
    }
}
