package com.thoughtworks.go.config;

import com.thoughtworks.go.config.materials.git.GitMaterial;
import com.thoughtworks.go.config.materials.git.GitMaterialConfig;
import com.thoughtworks.go.config.remote.ConfigRepoConfig;
import com.thoughtworks.go.config.remote.ConfigReposConfig;
import com.thoughtworks.go.domain.materials.Material;
import com.thoughtworks.go.helper.NoOpMetricsProbeService;
import com.thoughtworks.go.listener.ConfigChangedListener;
import com.thoughtworks.go.metrics.service.MetricsProbeService;
import com.thoughtworks.go.server.util.ServerVersion;
import com.thoughtworks.go.serverhealth.ServerHealthService;
import com.thoughtworks.go.service.ConfigRepository;
import com.thoughtworks.go.util.ConfigElementImplementationRegistryMother;
import com.thoughtworks.go.util.GoConfigFileHelper;
import com.thoughtworks.go.util.SystemEnvironment;
import com.thoughtworks.go.util.TimeProvider;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import static com.thoughtworks.go.helper.ConfigFileFixture.CONFIG;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.*;

/**
 * Created by tomzo on 6/15/15.
 */
public class GoConfigWatchListTest {

    private CachedFileGoConfig cachedGoConfig;
    private GoConfigWatchList watchList;
    private CruiseConfig cruiseConfig;

    @Before
    public void setUp() throws Exception {
        cachedGoConfig = mock(CachedFileGoConfig.class);
        cruiseConfig = mock(CruiseConfig.class);
        when(cachedGoConfig.currentConfig()).thenReturn(cruiseConfig);
        watchList = new GoConfigWatchList(cachedGoConfig);
    }

    @Test
    public void shouldNotifyConfigListenersWhenConfigChanges() throws Exception {
        final ChangedRepoConfigWatchListListener listener = mock(ChangedRepoConfigWatchListListener.class);

        watchList.registerListener(listener);

        when(cruiseConfig.getConfigRepos()).thenReturn(new ConfigReposConfig());
        watchList.onConfigChange(cruiseConfig);

        verify(listener, times(1)).onChangedRepoConfigWatchList(notNull(ConfigReposConfig.class));
    }

    @Test
    public void shouldReturnTrueWhenHasConfigRepoWithFingerprint()
    {
        GitMaterialConfig gitrepo = new GitMaterialConfig("http://configrepo.git");
        when(cruiseConfig.getConfigRepos()).thenReturn(new ConfigReposConfig(
                new ConfigRepoConfig(gitrepo,"myplugin")));

        watchList = new GoConfigWatchList(cachedGoConfig);

        assertTrue(watchList.hasConfigRepoWithFingerprint(gitrepo.getFingerprint()));
    }
    @Test
    public void shouldReturnFalseWhenDoesNotHaveConfigRepoWithFingerprint()
    {
        GitMaterialConfig gitrepo = new GitMaterialConfig("http://configrepo.git");
        when(cruiseConfig.getConfigRepos()).thenReturn(new ConfigReposConfig(
                new ConfigRepoConfig(gitrepo,"myplugin")));

        watchList = new GoConfigWatchList(cachedGoConfig);

        GitMaterialConfig gitrepo2 = new GitMaterialConfig("http://configrepo.git","dev");
        assertFalse(watchList.hasConfigRepoWithFingerprint(gitrepo2.getFingerprint()));
    }


    @Test
    public void shouldReturnConfigRepoForMaterial()
    {
        GitMaterialConfig gitrepo = new GitMaterialConfig("http://configrepo.git");
        ConfigRepoConfig repoConfig = new ConfigRepoConfig(gitrepo, "myplugin");
        when(cruiseConfig.getConfigRepos()).thenReturn(new ConfigReposConfig(
                repoConfig));

        watchList = new GoConfigWatchList(cachedGoConfig);

        assertThat(watchList.getConfigRepoForMaterial(gitrepo), is(repoConfig));
    }

}
