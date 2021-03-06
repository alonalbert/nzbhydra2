package org.nzbhydra.indexers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.nzbhydra.config.BaseConfig;
import org.nzbhydra.config.ConfigProvider;
import org.nzbhydra.config.indexer.IndexerConfig;
import org.nzbhydra.indexers.exceptions.IndexerSearchAbortedException;
import org.nzbhydra.searching.dtoseventsenums.SearchType;
import org.nzbhydra.searching.searchrequests.SearchRequest;
import org.nzbhydra.searching.searchrequests.SearchRequest.SearchSource;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AnizbTest {

    @Mock
    private ConfigProvider configProviderMock;
    BaseConfig baseConfig = new BaseConfig();
    @Mock
    private QueryGenerator queryGeneratorMock;

    @InjectMocks
    private Anizb testee = new Anizb();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(configProviderMock.getBaseConfig()).thenReturn(baseConfig);
        testee.config = new IndexerConfig();
        testee.config.setName("anizb");
        testee.config.setHost("https://anizb.org");
        when(queryGeneratorMock.generateQueryIfApplicable(any(), any(), any())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {

                final SearchRequest searchRequest = (SearchRequest) invocation.getArgument(0);
                if (searchRequest.getQuery().isPresent()) {
                    return searchRequest.getQuery().get();
                }
                return invocation.getArgument(1);
            }
        });
    }

    @Test
    public void shouldParseXml() {

    }

    @Test
    public void shouldBuildSimpleQuery() throws IndexerSearchAbortedException {
        SearchRequest searchRequest = new SearchRequest(SearchSource.INTERNAL, SearchType.SEARCH, 0, 100);
        searchRequest.setQuery("query");
        UriComponentsBuilder builder = testee.buildSearchUrl(searchRequest, 0, 100);
        assertThat(builder.toUriString(), is("https://anizb.org/api/?q=query"));
    }

    @Test
    public void shouldAddRequiredWords() throws IndexerSearchAbortedException {
        SearchRequest searchRequest = new SearchRequest(SearchSource.INTERNAL, SearchType.SEARCH, 0, 100);
        searchRequest.getInternalData().setRequiredWords(Arrays.asList("a", "b"));
        searchRequest.setQuery("query");
        UriComponentsBuilder builder = testee.buildSearchUrl(searchRequest, 0, 100);
        assertThat(builder.toUriString(), is("https://anizb.org/api/?q=query%20a%20b"));
    }

    @Test(expected = IndexerSearchAbortedException.class)
    public void shouldAbortIfSearchNotPossible() throws IndexerSearchAbortedException {
        SearchRequest searchRequest = new SearchRequest(SearchSource.INTERNAL, SearchType.SEARCH, 0, 100);
        testee.buildSearchUrl(searchRequest, 0, 100);
    }


}