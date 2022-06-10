package com.dxvkstatecachebank.dxvkstatecachebank;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.CacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.IncrementalCacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.User;
import com.dxvkstatecachebank.dxvkstatecachebank.repository.CacheFileRepository;
import com.dxvkstatecachebank.dxvkstatecachebank.repository.GameRepository;
import com.dxvkstatecachebank.dxvkstatecachebank.repository.IncrementalCacheRepository;
import com.dxvkstatecachebank.dxvkstatecachebank.repository.UserRepository;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HexFormat;

@Component
public class SampleDataCreator {
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final CacheFileRepository cacheFileRepository;
    private final IncrementalCacheRepository incrementalCacheRepository;

    private final byte[] example_image = HexFormat.of().parseHex("89504E470D0A1A0A0000000D4948445200000001000000010802000000907753DE00000185694343504943432070726F66696C65000028917D913D48C3401CC55F53B5522B22761071C8509D2C888A889356A1081542ADD0AA83C9A51F42938624C5C551702D38F8B158757071D6D5C15510043F401C9D9C145DA4C4FF258516B11E1CF7E3DDBDC7DD3B40A8169966B58D029A6E9BC9784C4C6756C4C02B3A10442FA6D12533CB9895A4045A8EAF7BF8F87A17E559ADCFFD39BAD5ACC5009F483CC30CD3265E279EDCB40DCEFBC461569055E273E211932E48FCC875C5E337CE7997059E193653C939E230B1986F62A58959C1D488278823AAA653BE90F658E5BCC5592B9659FD9EFC85A1ACBEBCC4759A838863018B90204241191B28C24694569D140B49DA8FB5F00FB87E895C0AB936C0C8318F1234C8AE1FFC0F7E776BE5C6C7BCA4500C687F719C8F2120B00BD42A8EF37DEC38B513C0FF0C5CE90D7FA90A4C7D925E69689123A0671BB8B86E68CA1E70B903F43F19B229BB929FA690CB01EF67F44D19A0EF1608AE7ABDD5F771FA00A4A8ABC40D7070080CE7297BADC5BB3B9B7BFBF74CBDBF1F6E1F72A5DF726BC1000000097048597300002E2300002E230178A53F760000000774494D4507E6060A113A23761E21B70000001974455874436F6D6D656E74004372656174656420776974682047494D5057810E170000000C4944415408D763F86FB20300042101EC43D5099A0000000049454E44AE426082");

    public SampleDataCreator(UserRepository userRepository, GameRepository gameRepository, CacheFileRepository cacheFileRepository, IncrementalCacheRepository incrementalCacheRepository) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.cacheFileRepository = cacheFileRepository;
        this.incrementalCacheRepository = incrementalCacheRepository;

        insertUsers();
        insertGames();
        insertCacheFiles();
        insertIncrementalCacheFiles();
    }

    public void insertUsers() {
        userRepository.save(
                User.builder()
                .id(1L)
                .email("abcd@gmail.com")
                .name("Spider Murphy")
                .password("password")
                .profilePicture(BlobProxy.generateProxy(example_image))
                .build()
        );

        userRepository.save(
                User.builder()
                .id(2L)
                .email("defd@example.com")
                .name("Eddie Murphy")
                .password("password1234")
                .profilePicture(BlobProxy.generateProxy(example_image))
                .build()
        );

        userRepository.save(
                User.builder()
                .id(3L)
                .email("deaaafd@yahoo.com")
                .name("Katie Jackson")
                .password("12345pass")
                .profilePicture(BlobProxy.generateProxy(example_image))
                .build()
        );
    }

    public void insertGames() {
        gameRepository.save(
                Game.builder()
                .id(1L)
                .name("Apex Legends")
                .steamId(1172470L)
                .build()
        );

        gameRepository.save(
                Game.builder()
                .id(2L)
                .name("Overwatch")
                .build()
        );
    }

    public void insertCacheFiles() {
        cacheFileRepository.save(
                CacheFile.builder()
                .id(1L)
                .uploadDateTime(LocalDateTime.now())
                .uploader(User.builder().id(2L).build())
                .game(Game.builder().id(1L).build())
                .data(BlobProxy.generateProxy(new byte[0]))
                .build()
        );
    }

    public void insertIncrementalCacheFiles() {
        incrementalCacheRepository.save(
                IncrementalCacheFile.builder()
                .id(2L)
                .game(Game.builder().id(2L).build())
                .lastUpdateTime(LocalDateTime.now())
                .data(BlobProxy.generateProxy(new byte[0]))
                .build()
        );
    }
}
