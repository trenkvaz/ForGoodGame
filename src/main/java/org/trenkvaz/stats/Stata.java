package org.trenkvaz.stats;

import java.io.Serializable;
import java.util.Map;

public class Stata implements Serializable {

    transient Map<String, MeansStata> stataMap;
    boolean isOnlyMainDB = false;
    public boolean isSelect = false;
    public boolean isMeans = false;
    public boolean isPreflopRange =false;


    public static class Builder {
        private final Stata stata;

        public Builder() {
            stata = new Stata();
        }

        public Builder isSelect() {
            stata.isSelect = true;
            return this;
        }

        public Builder isMeans() {
            stata.isMeans = true;
            return this;
        }

        public Builder isPreflopRange() {
            stata.isPreflopRange = true;
            return this;
        }

        public Builder isOnlyMainDB() {
            stata.isOnlyMainDB = true;
            return this;
        }

        public Stata build() {
            return stata;
        }
    }
}
