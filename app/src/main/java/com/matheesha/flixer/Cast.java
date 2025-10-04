package com.matheesha.flixer;

public class Cast {
    private String actorName;
    private String characterName;
    private int profileImageUrl;

    public Cast(String actorName, String characterName, int profileImageUrl) {
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

    public int getProfileImageUrl() {
        return profileImageUrl;
    }
}
