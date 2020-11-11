package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Artist extends Model {

    Long artistId;
    String name;
    String name1;

    public Artist() {

    }
    private Artist(ResultSet results) throws SQLException {
        name = results.getString("Name");
        artistId = results.getLong("ArtistId");
        name1 = results.getString("Name");
    }
    public List<Album> getAlbums() {
        return Album.getForArtist(artistId);
    }

    public Long getArtistId() {
        return artistId;
    }

    public void setArtist(Artist artist) {
        this.artistId = artist.getArtistId();
    }

    public String getName1() {
        return name1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;

    }

    public static List<Artist> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Artist> all(int page, int count) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM artists LIMIT ? OFFSET ?  "
             )) {
            stmt.setInt(1, count);
            stmt.setInt(2, (page - 1) * count);

            ResultSet results = stmt.executeQuery();
            List<Artist> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Artist(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static Artist find(long i) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM artists WHERE ArtistId=?")) {
            stmt.setLong(1, i);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Artist(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    @Override
    public boolean create() {
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO artists (name) VALUES (?)")) {
                stmt.setString(1, this.getName());

                stmt.executeUpdate();
                artistId = DB.getLastID(conn);
                return true;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean verify() {
        _errors.clear(); // clear any existing errors
        if (name == null || "".equals(name)) {
            addError("artist name can't be null or blank!");
        }

        return !hasErrors();
    }

    @Override
    public boolean update() {
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE artists SET name =? WHERE  ArtistId=? AND name=? ")) {
                stmt.setString(1, this.getName());
                stmt.setLong(2,this.getArtistId());
                stmt.setString(3,this.getName1());
             if(stmt.executeUpdate()==0) {


                 return false;
             }
             else{
                 stmt.executeUpdate();
                 return true;
             }
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

}