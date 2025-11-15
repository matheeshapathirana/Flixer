package com.matheesha.flixer;

public class Cast {
    private String actorName;
    private String characterName;
    private String profileImageUrl;

    public Cast(String actorName, String characterName, String profileImageUrl) {
        this.actorName = actorName;
        this.characterName = characterName;
        this.profileImageUrl = profileImageUrl;
    }

    public String getActorName() {
        return actorName;
    }

    public String getCharacterName() {
        return characterName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}
