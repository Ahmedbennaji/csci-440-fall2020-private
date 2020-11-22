package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Track extends Model {

    private Long trackId;
    private Long albumId;
    private Long mediaTypeId;
    private Long genreId;
    private String name;
    public String Artist;
     public String title;
    private Long milliseconds;
    private Long bytes;
    private BigDecimal unitPrice;

    public static final String REDIS_CACHE_KEY = "cs440-tracks-count-cache";

    public Track() {
        mediaTypeId = 1l;
        genreId = 1l;
        milliseconds  = 0l;
        bytes  = 0l;
        unitPrice = new BigDecimal("0");
    }

    Track(ResultSet results) throws SQLException {

        name = results.getString("Name");
        milliseconds = results.getLong("Milliseconds");
        bytes = results.getLong("Bytes");
        unitPrice = results.getBigDecimal("UnitPrice");
        trackId = results.getLong("TrackId");
        albumId = results.getLong("AlbumId");
        mediaTypeId = results.getLong("MediaTypeId");
        genreId = results.getLong("GenreId");
        title = results.getString("title");
        Artist = results.getString("Artist");
    }

    public static Track find(long i) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT tracks.*, Albums.Title as title, Artists.Name as Artist FROM tracks JOIN albums ON albums.AlbumID = tracks.AlbumID\n" +
                     "JOIN artists ON artists.ArtistId = albums.ArtistId\n" +
                     "WHERE TrackId =?")) {
            stmt.setLong(1, i);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Track(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static Long count() {
        Jedis redisClient = new Jedis(); // use this class to access redis and create a cache
        String stringValue= redisClient.get(REDIS_CACHE_KEY);
        redisClient.del(REDIS_CACHE_KEY);
        if(stringValue != null){
            return Long.parseLong(stringValue);
        }
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) as Count FROM tracks")) {
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
               results.getLong("Count");
               redisClient.set(REDIS_CACHE_KEY,Long.toString(results.getLong("Count")));
                return results.getLong("Count");


            } else {

                throw new IllegalStateException("Should find a count!");
            }

        } catch (SQLException sqlException) {



            throw new RuntimeException(sqlException);
        }

    }

    public Album getAlbum() {
        return Album.find(albumId);
    }

    public MediaType getMediaType() {
        return null;
    }
    public Genre getGenre() {
        return null;
    }
    public List<Playlist> getPlaylists(){

        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(

            " SELECT * FROM playlists\n" +
                    "    join playlist_track on " +
                    "playlists.PlaylistId = playlist_track.PlaylistId\n" +
                    "join tracks on" +
                    " tracks.TrackId = playlist_track.TrackId " +
                    "WHERE ? = playlist_track.TrackId AND playlists.PlaylistId = playlist_track.PlaylistId;"
             )) {

            stmt.setLong(1, this.getTrackId());
            ResultSet results = stmt.executeQuery();
            List<Playlist> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Playlist(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }


        //return Collections.emptyList();
    }

    public Long getTrackId() {
        return trackId;
    }

    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(Long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public void setAlbum(Album album) {
        albumId = album.getAlbumId();
    }

    public Long getMediaTypeId() {
        return mediaTypeId;
    }

    public void setMediaTypeId(Long mediaTypeId) {
        this.mediaTypeId = mediaTypeId;
    }

    public Long getGenreId() {
        return genreId;
    }

    public void setGenreId(Long genreId) {
        this.genreId = genreId;
    }

    public String getArtistName() {
        // TODO implement more efficiently
        //  hint: cache on this model object
        return Artist;
    }

    public String getAlbumTitle() {
        // TODO implement more efficiently
        // hint: cache on this model object
        return title;
   }

    public static List<Track> advancedSearch(int page, int count,
                                             String search, Integer artistId, Integer albumId,
                                             Integer maxRuntime, Integer minRuntime) {
        LinkedList<Object> args = new LinkedList<>();

        String query = "SELECT *,  FROM tracks" +
                " JOIN albums ON tracks.AlbumId = albums.AlbumId " +
                "WHERE name LIKE" +
        args.add("%" + search + "%");

        // Conditionally include the query and argument
        if (artistId != null) {
            query += " AND artists.ArtistId=? ";
            args.add(count);
        }

        query += " LIMIT ?";
        args.add(count);

        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < args.size(); i++) {
                Object arg = args.get(i);
                stmt.setObject(i + 1, arg);
            }
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Track> search(int page, int count, String orderBy, String search) {
        String query = "SELECT tracks.*, Albums.Title as title, Artists.Name as Artist FROM tracks" +
                " JOIN albums on albums.AlbumId = tracks.AlbumId" +
                "  JOIN artists on artists.ArtistId = albums.ArtistId" +
                "     WHERE tracks.name LIKE ? LIMIT ?";
        search = "%" + search + "%";
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, search);
            stmt.setInt(2, count);
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Track> forAlbum(Long albumId) {
        String query = "SELECT tracks.*, Albums.Title as title, Artists.Name as Artist \n" +
                " From tracks \n" +
                "JOIN albums on tracks.AlbumId = albums.AlbumId\n" +
                " JOIN artists on albums.ArtistId = artists.ArtistId\n" +
                "    WHERE tracks.AlbumId=?;";
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, albumId);
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    // Sure would be nice if java supported default parameter values
    public static List<Track> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Track> all(int page, int count) {
        return all(page, count, "TrackId");
    }

    public static List<Track> all(int page, int count, String orderBy) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(

" SELECT tracks.*, Albums.Title as title, Artists.Name as Artist FROM tracks \n" +
        "  JOIN artists ON artists.ArtistId = albums.ArtistId\n" +
        "   JOIN albums ON albums.AlbumID = tracks.AlbumID\n" +
        " ORDER BY " + orderBy + "  LIMIT  ? OFFSET ?"
             )) {
            stmt.setInt(1, count);
            stmt.setInt(2, (page - 1) * count);
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
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
                trackId = DB.getLastID(conn);
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
            addError("Tracks name can't be null or blank!");
        }
            if(albumId == null||"".equals(trackId)){
                addError("album can't be null or blank");
            }


        return !hasErrors();
    }
    @Override
    public boolean update() {
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE tracks SET name =?")) {
                stmt.setString(1, this.getName());
                stmt.executeUpdate();
                return true;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }



    public void delete() {
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "DELETE FROM tracks WHERE name = ?")) {
                stmt.setString(1, this.getName());

                stmt.executeUpdate();
                trackId = DB.getLastID(conn);

            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        }

    }


}
