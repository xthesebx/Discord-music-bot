package Discord.commandCreations;

import org.json.JSONArray;
import org.json.JSONObject;

public class BasicCommand extends JSONObject {


    public void setName(String name) {
        this.put("name", name);
    }

    public void setDescription(String description) {
        this.put("description", description);
    }

    public void setType(int type) {
        this.put("type", type);
    }

    public void setDescriptionLocalization (String language, String description) {
        if (!this.has("description_localizations"))
            this.put("description_localizations", new JSONObject().put(language, description));
        else {
            this.getJSONObject("description_localizations").put(language, description);
        }
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
