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
import java.sql.*;
import java.util.HexFormat;
import java.util.Objects;

public class V2__add_sample_data extends BaseJavaMigration {
    private final byte[] example_image = HexFormat.of().parseHex("89504E470D0A1A0A0000000D4948445200000001000000010802000000907753DE00000185694343504943432070726F66696C65000028917D913D48C3401CC55F53B5522B22761071C8509D2C888A889356A1081542ADD0AA83C9A51F42938624C5C551702D38F8B158757071D6D5C15510043F401C9D9C145DA4C4FF258516B11E1CF7E3DDBDC7DD3B40A8169966B58D029A6E9BC9784C4C6756C4C02B3A10442FA6D12533CB9895A4045A8EAF7BF8F87A17E559ADCFFD39BAD5ACC5009F483CC30CD3265E279EDCB40DCEFBC461569055E273E211932E48FCC875C5E337CE7997059E193653C939E230B1986F62A58959C1D488278823AAA653BE90F658E5BCC5592B9659FD9EFC85A1ACBEBCC4759A838863018B90204241191B28C24694569D140B49DA8FB5F00FB87E895C0AB936C0C8318F1234C8AE1FFC0F7E776BE5C6C7BCA4500C687F719C8F2120B00BD42A8EF37DEC38B513C0FF0C5CE90D7FA90A4C7D925E69689123A0671BB8B86E68CA1E70B903F43F19B229BB929FA690CB01EF67F44D19A0EF1608AE7ABDD5F771FA00A4A8ABC40D7070080CE7297BADC5BB3B9B7BFBF74CBDBF1F6E1F72A5DF726BC1000000097048597300002E2300002E230178A53F760000000774494D4507E6060A113A23761E21B70000001974455874436F6D6D656E74004372656174656420776974682047494D5057810E170000000C4944415408D763F86FB20300042101EC43D5099A0000000049454E44AE426082");

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
                var cacheFileResource = new ClassPathResource("sample/r5apex-barely-populated.dxvk-cache");
                ps.setBlob(4, BlobProxy.generateProxy(cacheFileResource.getInputStream(), cacheFileResource.contentLength()));
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
            ps.setString(1, "Overwatch");
            ps.setString(2, "Overwatch");
            if(steamId == null)
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
