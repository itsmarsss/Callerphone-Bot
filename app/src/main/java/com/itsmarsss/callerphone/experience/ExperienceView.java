package com.itsmarsss.callerphone.experience;

import java.util.List;

/**
 * Product response model. Domain services return results; presenters build views;
 * {@link ExperienceRenderer} turns views into Discord embeds and components.
 */
public record ExperienceView(
        ExperienceIntent intent,
        String title,
        String description,
        List<ViewField> fields,
        String footer,
        MediaSpec media,
        List<ActionSpec> actions,
        boolean ephemeral
) {
    public ExperienceView {
        fields = fields == null ? List.of() : List.copyOf(fields);
        actions = actions == null ? List.of() : List.copyOf(actions);
    }

    public static Builder builder(ExperienceIntent intent) {
        return new Builder(intent);
    }

    public static final class Builder {
        private final ExperienceIntent intent;
        private String title;
        private String description;
        private List<ViewField> fields = List.of();
        private String footer;
        private MediaSpec media;
        private List<ActionSpec> actions = List.of();
        private boolean ephemeral;

        private Builder(ExperienceIntent intent) {
            this.intent = intent;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder fields(List<ViewField> fields) {
            this.fields = fields;
            return this;
        }

        public Builder footer(String footer) {
            this.footer = footer;
            return this;
        }

        public Builder media(MediaSpec media) {
            this.media = media;
            return this;
        }

        public Builder actions(List<ActionSpec> actions) {
            this.actions = actions;
            return this;
        }

        public Builder actions(ActionSpec... actions) {
            this.actions = List.of(actions);
            return this;
        }

        public Builder ephemeral(boolean ephemeral) {
            this.ephemeral = ephemeral;
            return this;
        }

        public ExperienceView build() {
            return new ExperienceView(intent, title, description, fields, footer, media, actions, ephemeral);
        }
    }
}
