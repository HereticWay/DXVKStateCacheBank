package com.dxvkstatecachebank.dxvkstatecachebank;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.User;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileUploadDto;
import com.dxvkstatecachebank.dxvkstatecachebank.exceptions.NoNewCacheEntryException;
import com.dxvkstatecachebank.dxvkstatecachebank.exceptions.UnsuccessfulCacheMergeException;
import com.dxvkstatecachebank.dxvkstatecachebank.repository.GameRepository;
import com.dxvkstatecachebank.dxvkstatecachebank.service.CacheFileService;
import com.dxvkstatecachebank.dxvkstatecachebank.service.UserService;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HexFormat;

//@Component
public class SampleDataCreator {
    private final UserService userService;
    private final GameRepository gameService;
    private final CacheFileService cacheFileService;

    private final byte[] example_image = HexFormat.of().parseHex("89504E470D0A1A0A0000000D4948445200000001000000010802000000907753DE00000185694343504943432070726F66696C65000028917D913D48C3401CC55F53B5522B22761071C8509D2C888A889356A1081542ADD0AA83C9A51F42938624C5C551702D38F8B158757071D6D5C15510043F401C9D9C145DA4C4FF258516B11E1CF7E3DDBDC7DD3B40A8169966B58D029A6E9BC9784C4C6756C4C02B3A10442FA6D12533CB9895A4045A8EAF7BF8F87A17E559ADCFFD39BAD5ACC5009F483CC30CD3265E279EDCB40DCEFBC461569055E273E211932E48FCC875C5E337CE7997059E193653C939E230B1986F62A58959C1D488278823AAA653BE90F658E5BCC5592B9659FD9EFC85A1ACBEBCC4759A838863018B90204241191B28C24694569D140B49DA8FB5F00FB87E895C0AB936C0C8318F1234C8AE1FFC0F7E776BE5C6C7BCA4500C687F719C8F2120B00BD42A8EF37DEC38B513C0FF0C5CE90D7FA90A4C7D925E69689123A0671BB8B86E68CA1E70B903F43F19B229BB929FA690CB01EF67F44D19A0EF1608AE7ABDD5F771FA00A4A8ABC40D7070080CE7297BADC5BB3B9B7BFBF74CBDBF1F6E1F72A5DF726BC1000000097048597300002E2300002E230178A53F760000000774494D4507E6060A113A23761E21B70000001974455874436F6D6D656E74004372656174656420776974682047494D5057810E170000000C4944415408D763F86FB20300042101EC43D5099A0000000049454E44AE426082");

    public SampleDataCreator(UserService userRepository, GameRepository gameRepository, CacheFileService cacheFileService) throws IOException, NoNewCacheEntryException, SQLException, UnsuccessfulCacheMergeException {
        this.userService = userRepository;
        this.gameService = gameRepository;
        this.cacheFileService = cacheFileService;

        insertUsers();
        insertGames();
        insertCacheFiles();
    }

    public void insertUsers() {
        userService.save(
                User.builder()
                        .id(1L)
                        .email("abcd@gmail.com")
                        .name("Spider Murphy")
                        .password("password")
                        .profilePicture(BlobProxy.generateProxy(example_image))
                        .build()
        );

        userService.save(
                User.builder()
                        .id(2L)
                        .email("defd@example.com")
                        .name("Eddie Murphy")
                        .password("password1234")
                        .profilePicture(BlobProxy.generateProxy(example_image))
                        .build()
        );

        userService.save(
                User.builder()
                        .id(3L)
                        .email("deaaafd@yahoo.com")
                        .name("Katie Jackson")
                        .password("12345pass")
                        .profilePicture(BlobProxy.generateProxy(example_image))
                        .build()
        );

        userService.flush();
    }

    public void insertGames() throws IOException {
        var apexCacheFileResource = new ClassPathResource("sample/r5apex.dxvk-cache"); // A real cache file
        try (var apexCacheFileInputStream = new BufferedInputStream(apexCacheFileResource.getInputStream())) {
            gameService.save(
                    Game.builder()
                            .id(1L)
                            .name("Apex Legends")
                            .cacheFileName("r5apex")
                            .incrementalCacheFile(BlobProxy.generateProxy(apexCacheFileInputStream, apexCacheFileResource.contentLength()))
                            .incrementalCacheLastModified(LocalDateTime.now())
                            .steamId(1172470L)
                            .build()
            );
        }

        gameService.save(
                Game.builder()
                        .id(2L)
                        .name("Overwatch")
                        .cacheFileName("Overwatch")
                        .incrementalCacheFile(null)
                        .steamId(null)
                        .build()
        );

        gameService.flush();
    }

    @Transactional
    public void insertCacheFiles() throws IOException, NoNewCacheEntryException, SQLException, UnsuccessfulCacheMergeException {
        var apexCacheFileResource = new ClassPathResource("sample/r5apex-highly-populated.dxvk-cache");
        var apexCacheFileInputStream = new BufferedInputStream(apexCacheFileResource.getInputStream());
        CacheFileUploadDto cacheFileUploadDto =
                CacheFileUploadDto.builder()
                        .uploaderId(2L)
                        .gameId(1L)
                        .build();

        cacheFileService.mergeCacheFileToIncrementalCacheAndSave(cacheFileUploadDto, apexCacheFileInputStream, apexCacheFileResource.contentLength());
        cacheFileService.flush();
    }
}
