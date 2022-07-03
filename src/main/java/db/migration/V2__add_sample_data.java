package db.migration;

import aj.org.objectweb.asm.Type;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.Objects;

public class V2__add_sample_data extends BaseJavaMigration {
    public void migrate(Context context) {
        DataSource dataSource = new SingleConnectionDataSource(context.getConnection(), true);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        var sampleProfilePicture1 = new ClassPathResource("sample/profile_pic_1.jpg");
        var sampleProfilePicture2 = new ClassPathResource("sample/profile_pic_2.jpg");

        long user1Id = insertUserWithProfilePicture(jdbcTemplate, "abcfd@gmail.com", "Spider Murphy", "password", sampleProfilePicture1);
        insertUserWithProfilePicture(jdbcTemplate, "defd@example.com", "Eddie Murphy", "password1234", sampleProfilePicture2);

        var apexCacheFileResource = new ClassPathResource("sample/r5apex-barely-populated.dxvk-cache");
        long gameApexId = insertGameWithIncrementalCache(jdbcTemplate, "Apex Legends", "r5apex", 1172470L, apexCacheFileResource);
        insertGameWithoutIncrementalCache(jdbcTemplate, "Overwatch", "Overwatch", null);

        insertCacheFile(jdbcTemplate, gameApexId, user1Id, apexCacheFileResource);
    }

    private long insertUserWithProfilePicture(JdbcTemplate jdbcTemplate, String email, String name, String password, Resource profilePicture) {
        String sql =
                "INSERT INTO \"user\" (email, \"name\", \"password\", profile_picture) VALUES (?, ?, ?, ?);";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, email);
            ps.setString(2, name);
            ps.setString(3, password);
            try {
                ps.setBlob(4, BlobProxy.generateProxy(profilePicture.getInputStream(), profilePicture.contentLength()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return ps;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    private long insertGameWithIncrementalCache(JdbcTemplate jdbcTemplate, String name, String cacheFileName, long steamId, Resource incrementalCacheFile) {
        String sql =
                "INSERT INTO game (\"name\", cache_file_name, steam_id, incremental_cache_file, incremental_cache_last_modified) VALUES (?, ?, ?, ?, NOW());";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, name);
            ps.setString(2, cacheFileName);
            ps.setLong(3, steamId);
            try {
                ps.setBlob(4, BlobProxy.generateProxy(incrementalCacheFile.getInputStream(), incrementalCacheFile.contentLength()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return ps;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    private void insertGameWithoutIncrementalCache(JdbcTemplate jdbcTemplate, String name, String cacheFileName, Long steamId) {
        String sql =
                "INSERT INTO game (\"name\", cache_file_name, steam_id, incremental_cache_file, incremental_cache_last_modified) VALUES (?, ?, ?, NULL, NULL);";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, cacheFileName);
            if (steamId == null)
                ps.setNull(3, Type.LONG);
            else
                ps.setLong(3, steamId);
            return ps;
        });
    }

    private void insertCacheFile(JdbcTemplate jdbcTemplate, Long gameId, Long uploaderId, Resource cacheFileResource) {
        String sql =
                "INSERT INTO cache_file (game_id, uploader_id, \"data\", upload_date) VALUES (?, ?, ?, NOW())";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, gameId);
            ps.setLong(2, uploaderId);
            try {
                ps.setBlob(3, BlobProxy.generateProxy(cacheFileResource.getInputStream(), cacheFileResource.contentLength()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return ps;
        });
    }
}
