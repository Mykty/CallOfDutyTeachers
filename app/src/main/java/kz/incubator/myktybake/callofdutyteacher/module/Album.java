package kz.incubator.myktybake.callofdutyteacher.module;

public class Album {
    private String name;
    private String numOfSongs;
    private int thumbnail;

    public Album() {
    }

    public Album(String name, String numOfSongs) {
        this.name = name;
        this.numOfSongs = numOfSongs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumOfSongs() {
        return numOfSongs;
    }

    public void setNumOfSongs(String numOfSongs) {
        this.numOfSongs = numOfSongs;
    }

    public int getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(int thumbnail) {
        this.thumbnail = thumbnail;
    }
}