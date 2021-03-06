// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.search.models.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CustomAnalyzerSyncTests extends SearchServiceTestBase {
    private static final String NAME_PREFIX = "azsmnet";

    private SearchServiceClient searchServiceClient;
    private static Collection<CharFilterName> charFilterNames;

    static {
        getAllCharFilterName();
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        searchServiceClient = getSearchServiceClientBuilder().buildClient();
    }

    private static void getAllCharFilterName() {
        charFilterNames = new ArrayList<>();
        for (CharFilterName name : CharFilterName.values()) {
            charFilterNames.add(name);
        }
    }

    @Test
    public void canSearchWithCustomAnalyzer() {
        final AnalyzerName customAnalyzerName = AnalyzerName.fromString("my_email_analyzer");
        final CharFilterName customCharFilterName = CharFilterName.fromString("my_email_filter");

        Index index = new Index()
            .setName("testindex")
            .setFields(Arrays.asList(
                new Field()
                    .setName("id")
                    .setType(DataType.EDM_STRING)
                    .setKey(true),
                new Field()
                    .setName("message")
                    .setType(DataType.EDM_STRING)
                    .setAnalyzer(customAnalyzerName)
                    .setSearchable(true)
            ))
            .setAnalyzers(Collections.singletonList(
                new CustomAnalyzer()
                    .setTokenizer(TokenizerName.STANDARD)
                    .setCharFilters(Collections.singletonList(customCharFilterName))
                    .setName(customAnalyzerName.toString())
            ))
            .setCharFilters(Collections.singletonList(
                new PatternReplaceCharFilter()
                    .setPattern("@")
                    .setReplacement("_")
                    .setName(customCharFilterName.toString())
            ));

        searchServiceClient.createIndex(index);

        SearchIndexClient searchIndexClient = searchServiceClient.getIndexClient(index.getName());

        SearchDocument document1 = new SearchDocument();
        document1.put("id", "1");
        document1.put("message", "My email is someone@somewhere.something.");
        SearchDocument document2 = new SearchDocument();
        document2.put("id", "2");
        document2.put("message", "His email is someone@nowhere.nothing.");
        List<SearchDocument> documents = Arrays.asList(document1, document2);

        searchIndexClient.uploadDocuments(documents);
        waitForIndexing();

        Iterator<SearchResult> iterator = searchIndexClient
            .search("someone@somewhere.something", new SearchOptions(), generateRequestOptions(), Context.NONE)
            .iterator();
        SearchResult searchResult = iterator.next();

        assertEquals("1", searchResult.getDocument().get("id"));
        assertFalse(iterator.hasNext());
    }

    @Test
    public void canUseAllAnalyzerNamesInIndexDefinition() {
        Index index = prepareIndexWithAllAnalyzerNames();

        Index res = searchServiceClient.createIndex(index);
        TestHelpers.assertIndexesEqual(index, res);

        // Add language analyzers to searchAnalyzer and indexAnalyzer properties and expect failure
        try {
            new Field()
                .setName("field")
                .setType(DataType.EDM_STRING)
                .setSearchAnalyzer(AnalyzerName.EN_LUCENE);
        } catch (Exception ex) {
            assertEquals(IllegalArgumentException.class, ex.getClass());
            assertEquals("Only non-language analyzer can be used as search analyzer.", ex.getMessage());
        }
        try {
            new Field()
                .setName("field")
                .setType(DataType.EDM_STRING)
                .setIndexAnalyzer(AnalyzerName.AR_MICROSOFT);
        } catch (Exception ex) {
            assertEquals(IllegalArgumentException.class, ex.getClass());
            assertEquals("Only non-language analyzer can be used as index analyzer.", ex.getMessage());
        }
    }

    @Test
    public void canAnalyze() {
        Index index = createTestIndex();
        searchServiceClient.createIndex(index);

        AnalyzeRequest request = new AnalyzeRequest()
            .setText("One two")
            .setAnalyzer(AnalyzerName.WHITESPACE);
        PagedIterable<TokenInfo> results = searchServiceClient.analyzeText(index.getName(), request);
        Iterator<TokenInfo> iterator = results.iterator();
        assertTokenInfoEqual("One", 0, 3, 0, iterator.next());
        assertTokenInfoEqual("two", 4, 7, 1, iterator.next());
        assertFalse(iterator.hasNext());

        request = new AnalyzeRequest()
            .setText("One's <two/>")
            .setTokenizer(TokenizerName.WHITESPACE)
            .setTokenFilters(Collections.singletonList(TokenFilterName.APOSTROPHE))
            .setCharFilters(Collections.singletonList(CharFilterName.HTML_STRIP));
        results = searchServiceClient.analyzeText(index.getName(), request);
        // End offset is based on the original token, not the one emitted by the filters.
        iterator = results.iterator();
        assertTokenInfoEqual("One", 0, 5, 0, iterator.next());
        assertFalse(iterator.hasNext());

        results = searchServiceClient.analyzeText(index.getName(), request, generateRequestOptions(), Context.NONE);
        // End offset is based on the original token, not the one emitted by the filters.
        iterator = results.iterator();
        assertTokenInfoEqual("One", 0, 5, 0, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void canAnalyzeWithAllPossibleNames() {
        Index index = createTestIndex();
        searchServiceClient.createIndex(index);

        AnalyzerName.values()
            .stream()
            .map(an -> new AnalyzeRequest()
                .setText("One two")
                .setAnalyzer(an))
            .forEach(r -> searchServiceClient.analyzeText(index.getName(), r));

        TokenizerName.values()
            .stream()
            .map(tn -> new AnalyzeRequest()
                .setText("One two")
                .setTokenizer(tn))
            .forEach(r -> searchServiceClient.analyzeText(index.getName(), r));

        AnalyzeRequest request = new AnalyzeRequest()
            .setText("One two")
            .setTokenizer(TokenizerName.WHITESPACE)
            .setTokenFilters(new ArrayList<>(TokenFilterName.values()))
            .setCharFilters(new ArrayList<>(CharFilterName.values()));
        searchServiceClient.analyzeText(index.getName(), request);
    }

    @Test
    public void addingCustomAnalyzerThrowsHttpExceptionByDefault() {
        Index index = createTestIndex()
            .setAnalyzers(Collections.singletonList(new StopAnalyzer().setName("a1")));
        searchServiceClient.createIndex(index);

        addAnalyzerToIndex(index, new StopAnalyzer().setName("a2"));

        assertHttpResponseException(
            () -> searchServiceClient.createOrUpdateIndex(index),
            HttpResponseStatus.BAD_REQUEST,
            "Index update not allowed because it would cause downtime."
        );
    }

    @Test
    public void canAddCustomAnalyzerWithIndexDowntime() {
        Index index = createTestIndex()
            .setAnalyzers(Collections.singletonList(new StopAnalyzer().setName("a1")));
        searchServiceClient.createIndex(index);

        addAnalyzerToIndex(index, new StopAnalyzer().setName("a2"));
        Index updatedIndex = searchServiceClient.createOrUpdateIndexWithResponse(index,
            true, new MatchConditions(), generateRequestOptions(), Context.NONE).getValue();

        assertAnalysisComponentsEqual(index, updatedIndex);
    }

    @Test
    public void canCreateAllAnalysisComponents() {
        Index index = prepareIndexWithAllAnalysisComponentTypes();

        Index createdIndex = searchServiceClient.createIndex(index);
        assertAnalysisComponentsEqual(index, createdIndex);
        searchServiceClient.deleteIndex(index.getName());

        // We have to split up analysis components into two indexes, one where any components with optional properties
        // have defaults that are zero or null, and another where we need to specify the default values we
        // expect to get back from the REST API.

        Index indexWithSpecialDefaults = createIndexWithSpecialDefaults();
        Index expectedIndexWithSpecialDefaults = createExpectedIndexWithSpecialDefaults(indexWithSpecialDefaults);

        List<Index> splittedIndexWithSpecialDefaults = splitIndex(indexWithSpecialDefaults);
        List<Index> splittedExpectedIndexWithSpecialDefaults = splitIndex(expectedIndexWithSpecialDefaults);
        for (int j = 0; j < splittedIndexWithSpecialDefaults.size(); j++) {
            Index expected = splittedExpectedIndexWithSpecialDefaults.get(j);
            Index actual = searchServiceClient.createIndex(expected);
            assertAnalysisComponentsEqual(expected, actual);
            searchServiceClient.deleteIndex(actual.getName());
        }
    }

    @Test
    public void canUseAllAnalysisComponentNames() {
        Index index = prepareIndexWithAllAnalysisComponentNames();

        Index createdIndex = searchServiceClient.createIndex(index);
        assertCustomAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlags() {
        Index index = createTestIndex()
            .setAnalyzers(RegexFlags.values()
                .stream()
                .map(rf -> new PatternAnalyzer()
                    .setLowerCaseTerms(true)
                    .setPattern(".*")
                    .setFlags(rf)
                    .setName(generateName()))
                .collect(Collectors.toList()));

        Index createdIndex = searchServiceClient.createIndex(index);

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllAnalysisComponentOptions() {
        List<Index> indexes = prepareIndexesWithAllAnalysisComponentOptions();

        indexes.forEach(expectedIndex -> {
            Index createdIndex = searchServiceClient.createIndex(expectedIndex);
            assertAnalysisComponentsEqual(expectedIndex, createdIndex);
            searchServiceClient.deleteIndex(createdIndex.getName());
        });
    }

    void addAnalyzerToIndex(Index index, Analyzer analyzer) {
        List<Analyzer> analyzers = new ArrayList<>(index.getAnalyzers());
        analyzers.add(analyzer);

        index.setAnalyzers(analyzers);
    }

    void assertAnalysisComponentsEqual(Index expected, Index actual) {
        // Compare analysis components directly so that test failures show better comparisons.
        // Analyzers
        assertAnalyzersEqual(expected.getAnalyzers(), actual.getAnalyzers());

        // Tokenizer
        assertTokenizersEqual(expected.getTokenizers(), actual.getTokenizers());

        // Char filter
        assertCharFiltersEqual(expected.getCharFilters(), actual.getCharFilters());
    }

    void assertCustomAnalysisComponentsEqual(Index expected, Index actual) {
        // Compare analysis components directly so that test failures show better comparisons.
        // Analyzers - Sort according to their Tokenizers before comparing:
        List<Analyzer> expectedAnalyzers = expected.getAnalyzers();
        List<Analyzer> actualAnalyzers = actual.getAnalyzers();

        if (expectedAnalyzers != null && actualAnalyzers != null) {
            Comparator<Analyzer> customAnalyzerComparator = Comparator
                .comparing((Analyzer a) -> ((CustomAnalyzer) a).getTokenizer().toString());

            expectedAnalyzers.sort(customAnalyzerComparator);
            actualAnalyzers.sort(customAnalyzerComparator);

            assertAnalyzersEqual(expectedAnalyzers, actualAnalyzers);
        }

        // Tokenizer
        assertTokenizersEqual(expected.getTokenizers(), actual.getTokenizers());

        // Char filter
        assertCharFiltersEqual(expected.getCharFilters(), actual.getCharFilters());
    }

    private void assertAnalyzersEqual(List<Analyzer> expected, List<Analyzer> actual) {
        if (expected != null && actual != null) {
            assertEquals(expected.size(), actual.size());
            for (int i = 0; i < expected.size(); i++) {

                ReflectionAssert.assertLenientEquals(
                    expected.get(i).setName("none"), actual.get(i).setName("none"));
            }
        }
    }

    private void assertTokenizersEqual(List<Tokenizer> expected, List<Tokenizer> actual) {
        if (expected != null && actual != null) {
            assertEquals(expected.size(), actual.size());
            for (int i = 0; i < expected.size(); i++) {
                ReflectionAssert.assertLenientEquals(
                    expected.get(i).setName("none"), actual.get(i).setName("none"));
            }
        }
    }

    private void assertCharFiltersEqual(List<CharFilter> expected, List<CharFilter> actual) {
        if (expected != null && actual != null) {
            assertEquals(expected.size(), actual.size());
            for (int i = 0; i < expected.size(); i++) {
                ReflectionAssert.assertLenientEquals(
                    expected.get(i).setName("none"), actual.get(i).setName("none"));
            }
        }
    }

    String generateName() {
        return testResourceNamer.randomName(NAME_PREFIX, 24);
    }

    List<Index> prepareIndexesWithAllAnalysisComponentOptions() {
        Index index = createTestIndex();

        // Set tokenizers
        List<Tokenizer> tokenizers = new ArrayList<>();
        tokenizers.add(
            new EdgeNGramTokenizer()
                .setMinGram(1)
                .setMaxGram(2)
                .setTokenChars(Arrays.asList(TokenCharacterKind.values()))
                .setName(generateName())
        );
        tokenizers.addAll(
            Arrays.stream(MicrosoftStemmingTokenizerLanguage.values())
                .map(mtl -> new MicrosoftLanguageStemmingTokenizer()
                    .setMaxTokenLength(200)
                    .setIsSearchTokenizer(false)
                    .setLanguage(mtl)
                    .setName(generateName())
                )
                .collect(Collectors.toList())
        );
        index.setTokenizers(tokenizers);

        // Set token filters
        List<TokenFilter> tokenFilters = new ArrayList<>();
        tokenFilters.add(new CjkBigramTokenFilter()
            .setIgnoreScripts(Arrays.asList(CjkBigramTokenFilterScripts.values()))
            .setOutputUnigrams(true)
            .setName(generateName()));
        tokenFilters.addAll(
            Arrays.stream(EdgeNGramTokenFilterSide.values())
                .map(s -> new EdgeNGramTokenFilterV2()
                    .setMinGram(1)
                    .setMaxGram(2)
                    .setSide(s)
                    .setName(generateName())
                )
                .collect(Collectors.toList())
        );
        tokenFilters.addAll(
            Arrays.stream(PhoneticEncoder.values())
                .map(pe -> new PhoneticTokenFilter()
                    .setEncoder(pe)
                    .setReplaceOriginalTokens(false)
                    .setName(generateName())
                )
                .collect(Collectors.toList())
        );
        tokenFilters.addAll(
            Arrays.stream(SnowballTokenFilterLanguage.values())
                .map(l -> new SnowballTokenFilter()
                    .setLanguage(l)
                    .setName(generateName())
                )
                .collect(Collectors.toList())
        );
        tokenFilters.addAll(
            Arrays.stream(StemmerTokenFilterLanguage.values())
                .map(l -> new StemmerTokenFilter()
                    .setLanguage(l)
                    .setName(generateName())
                )
                .collect(Collectors.toList())
        );
        tokenFilters.addAll(
            Arrays.stream(StopwordsList.values())
                .map(l -> new StopwordsTokenFilter()
                    .setStopwordsList(l)
                    .setIgnoreCase(false)
                    .setRemoveTrailingStopWords(true)
                    .setName(generateName())
                )
                .collect(Collectors.toList())
        );
        index.setTokenFilters(tokenFilters);

        return splitIndex(index);
    }

    Index prepareIndexWithAllAnalyzerNames() {
        List<AnalyzerName> allAnalyzerNames = new ArrayList<>(AnalyzerName.values());
        allAnalyzerNames.sort(Comparator.comparing(AnalyzerName::toString));

        List<Field> fields = new ArrayList<>();
        int fieldNumber = 0;

        // All analyzer names can be set on the analyzer property.
        for (int i = 0; i < allAnalyzerNames.size(); i++) {
            DataType fieldType = (i % 2 == 0) ? DataType.EDM_STRING : DataType.collection(DataType.EDM_STRING);
            fields.add(new Field()
                .setName("field" + (fieldNumber++))
                .setType(fieldType)
                .setAnalyzer(allAnalyzerNames.get(i)));
        }

        List<AnalyzerName> searchAnalyzersAndIndexAnalyzers = getAnalyzersAllowedForSearchAnalyzerAndIndexAnalyzer();

        for (int i = 0; i < searchAnalyzersAndIndexAnalyzers.size(); i++) {
            DataType fieldType = (i % 2 == 0) ? DataType.EDM_STRING : DataType.collection(DataType.EDM_STRING);
            fields.add(new Field()
                .setName("field" + (fieldNumber++))
                .setType(fieldType)
                .setSearchable(true)
                .setSearchAnalyzer(searchAnalyzersAndIndexAnalyzers.get(i))
                .setIndexAnalyzer(searchAnalyzersAndIndexAnalyzers.get(i)));
        }

        fields.add(new Field()
            .setName("id")
            .setType(DataType.EDM_STRING)
            .setKey(true));

        return new Index()
            .setName("hotel")
            .setFields(fields);
    }

    Index prepareIndexWithAllAnalysisComponentNames() {
        Analyzer analyzerWithAllTokenFilterAndCharFilters =
            new CustomAnalyzer()
                .setTokenizer(TokenizerName.LOWERCASE)
                .setTokenFilters(TokenFilterName.values()
                    .stream()
                    .sorted(Comparator.comparing(TokenFilterName::toString))
                    .collect(Collectors.toList()))
                .setCharFilters(charFilterNames
                    .stream()
                    .sorted(Comparator.comparing(CharFilterName::toString))
                    .collect(Collectors.toList()))
                .setName("abc");

        Index index = createTestIndex();
        List<Analyzer> analyzers = new ArrayList<>();
        analyzers.add(analyzerWithAllTokenFilterAndCharFilters);
        analyzers.addAll(TokenizerName.values()
            .stream()
            .sorted(Comparator.comparing(TokenizerName::toString))
            .map(tn -> new CustomAnalyzer()
                .setTokenizer(tn)
                .setName(generateName()))
            .collect(Collectors.toList()));

        analyzers.sort(Comparator.comparing(Analyzer::getName));
        index.setAnalyzers(analyzers);

        return index;
    }

    /**
     * Custom analysis components (analyzer/tokenzier/tokenFilter/charFilter) count in index must be between 0 and 50.
     * Split an Index into indexes, each of which has a total analysis components count within the limit.
     */
    List<Index> splitIndex(Index index) {
        Collection<List<Analyzer>> analyzersLists = splitAnalysisComponents(index.getAnalyzers());
        List<Index> indexes = analyzersLists
            .stream()
            .map(a -> createTestIndex().setAnalyzers(a)).collect(Collectors.toList());

        Collection<List<Tokenizer>> tokenizersLists = splitAnalysisComponents(index.getTokenizers());
        indexes.addAll(tokenizersLists
            .stream()
            .map(t -> createTestIndex().setTokenizers(t))
            .collect(Collectors.toList()));

        Collection<List<TokenFilter>> tokenFiltersLists = splitAnalysisComponents(index.getTokenFilters());
        indexes.addAll(tokenFiltersLists
            .stream()
            .map(tf -> createTestIndex().setTokenFilters(tf))
            .collect(Collectors.toList()));

        Collection<List<CharFilter>> charFiltersLists = splitAnalysisComponents(index.getCharFilters());
        indexes.addAll(charFiltersLists
            .stream()
            .map(cf -> createTestIndex().setCharFilters(cf))
            .collect(Collectors.toList()));

        return indexes;
    }

    /**
     * Custom analysis components (analyzer/tokenzier/tokenFilter/charFilter) count in index must be between 0 and 50.
     * Split a list of analysis components into lists within the limit.
     */
    private <T> Collection<List<T>> splitAnalysisComponents(List<T> list) {
        final int analysisComponentLimit = 50;
        Collection<List<T>> lists = new HashSet<>();

        if (list != null && !list.isEmpty()) {
            if (list.size() > analysisComponentLimit) {
                AtomicInteger counter = new AtomicInteger();
                lists = list.stream()
                    .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / analysisComponentLimit))
                    .values();
            } else {
                lists.add(list);
            }
        }

        return lists;
    }

    Index prepareIndexWithAllAnalysisComponentTypes() {
        final TokenizerName customTokenizerName = TokenizerName.fromString("my_tokenizer");
        final TokenFilterName customTokenFilterName = TokenFilterName.fromString("my_tokenfilter");
        final CharFilterName customCharFilterName = CharFilterName.fromString("my_charfilter");

        return createTestIndex()
            .setAnalyzers(Arrays.asList(
                new CustomAnalyzer()
                    .setTokenizer(customTokenizerName)
                    .setTokenFilters(Collections.singletonList(customTokenFilterName))
                    .setCharFilters(Collections.singletonList(customCharFilterName))
                    .setName(generateName()),
                new CustomAnalyzer()
                    .setTokenizer(TokenizerName.EDGE_NGRAM)
                    .setName(generateName()),
                new PatternAnalyzer()
                    .setLowerCaseTerms(false)
                    .setPattern("abc")
                    .setFlags(RegexFlags.DOTALL)
                    .setStopwords(Collections.singletonList("the"))
                    .setName(generateName()),
                new StandardAnalyzer()
                    .setMaxTokenLength(100)
                    .setStopwords(Collections.singletonList("the"))
                    .setName(generateName()),
                new StopAnalyzer()
                    .setStopwords(Collections.singletonList("the"))
                    .setName(generateName()),
                new StopAnalyzer()
                    .setName(generateName())
            ))
            .setTokenizers(Arrays.asList(
                new EdgeNGramTokenizer()
                    .setMinGram(1)
                    .setMaxGram(2)
                    .setName(customTokenizerName.toString()),
                new EdgeNGramTokenizer()
                    .setMinGram(2)
                    .setMaxGram(4)
                    .setTokenChars(Collections.singletonList(TokenCharacterKind.LETTER))
                    .setName(generateName()),
                new NGramTokenizer()
                    .setMinGram(2)
                    .setMaxGram(4)
                    .setTokenChars(Collections.singletonList(TokenCharacterKind.LETTER))
                    .setName(generateName()),
                new ClassicTokenizer()
                    .setMaxTokenLength(100)
                    .setName(generateName()),
                new KeywordTokenizerV2()
                    .setMaxTokenLength(100)
                    .setName(generateName()),
                new MicrosoftLanguageStemmingTokenizer()
                    .setMaxTokenLength(100)
                    .setIsSearchTokenizer(true)
                    .setLanguage(MicrosoftStemmingTokenizerLanguage.CROATIAN)
                    .setName(generateName()),
                new MicrosoftLanguageTokenizer()
                    .setMaxTokenLength(100)
                    .setIsSearchTokenizer(true)
                    .setLanguage(MicrosoftTokenizerLanguage.THAI)
                    .setName(generateName()),
                new PathHierarchyTokenizerV2()
                    .setDelimiter(":")
                    .setReplacement("_")
                    .setMaxTokenLength(300)
                    .setReverseTokenOrder(true)
                    .setNumberOfTokensToSkip(2)
                    .setName(generateName()),
                new PatternTokenizer()
                    .setPattern(".*")
                    .setFlags(RegexFlags.MULTILINE)
                    .setGroup(0)
                    .setName(generateName()),
                new StandardTokenizerV2()
                    .setMaxTokenLength(100)
                    .setName(generateName()),
                new UaxUrlEmailTokenizer()
                    .setMaxTokenLength(100)
                    .setName(generateName())
            ))
            .setTokenFilters(Arrays.asList(
                new CjkBigramTokenFilter()
                    .setName(customTokenFilterName.toString()),  // One custom token filter for CustomAnalyzer above.
                new CjkBigramTokenFilter()
                    .setIgnoreScripts(Collections.singletonList(CjkBigramTokenFilterScripts.HAN))
                    .setOutputUnigrams(true)
                    .setName(generateName()),
                new CjkBigramTokenFilter()
                    .setName(generateName()),
                new AsciiFoldingTokenFilter()
                    .setPreserveOriginal(true)
                    .setName(generateName()),
                new AsciiFoldingTokenFilter()
                    .setName(generateName()),
                new CommonGramTokenFilter()
                    .setCommonWords(Arrays.asList("hello", "goodbye"))
                    .setIgnoreCase(true)
                    .setUseQueryMode(true)
                    .setName(generateName()),
                new CommonGramTokenFilter()
                    .setCommonWords(Collections.singletonList("at"))
                    .setName(generateName()),
                new DictionaryDecompounderTokenFilter()
                    .setWordList(Collections.singletonList("Schadenfreude"))
                    .setMinWordSize(10)
                    .setMinSubwordSize(5)
                    .setMaxSubwordSize(13)
                    .setOnlyLongestMatch(true)
                    .setName(generateName()),
                new EdgeNGramTokenFilterV2()
                    .setMinGram(2)
                    .setMaxGram(10)
                    .setSide(EdgeNGramTokenFilterSide.BACK)
                    .setName(generateName()),
                new ElisionTokenFilter()
                    .setArticles(Collections.singletonList("a"))
                    .setName(generateName()),
                new ElisionTokenFilter()
                    .setName(generateName()),
                new KeepTokenFilter()
                    .setKeepWords(Collections.singletonList("aloha"))
                    .setName(generateName()),
                new KeepTokenFilter()
                    .setKeepWords(Arrays.asList("e", "komo", "mai"))
                    .setName(generateName()),
                new KeywordMarkerTokenFilter()
                    .setKeywords(Arrays.asList("key", "words"))
                    .setName(generateName()),
                new KeywordMarkerTokenFilter()
                    .setKeywords(Collections.singletonList("essential"))
                    .setName(generateName()),
                new LengthTokenFilter()
                    .setMin(5)
                    .setMax(10)
                    .setName(generateName()),
                new LimitTokenFilter()
                    .setMaxTokenCount(10)
                    .setConsumeAllTokens(true)
                    .setName(generateName()),
                new NGramTokenFilterV2()
                    .setMinGram(2)
                    .setMaxGram(3)
                    .setName(generateName()),
                new PatternCaptureTokenFilter()
                    .setPatterns(Collections.singletonList(".*"))
                    .setPreserveOriginal(false)
                    .setName(generateName()),
                new PatternReplaceTokenFilter()
                    .setPattern("abc")
                    .setReplacement("123")
                    .setName(generateName()),
                new PhoneticTokenFilter()
                    .setEncoder(PhoneticEncoder.SOUNDEX)
                    .setReplaceOriginalTokens(false)
                    .setName(generateName()),
                new ShingleTokenFilter()
                    .setMaxShingleSize(10)
                    .setMinShingleSize(5)
                    .setOutputUnigrams(false)
                    .setOutputUnigramsIfNoShingles(true)
                    .setTokenSeparator(" ")
                    .setFilterToken("|")
                    .setName(generateName()),
                new SnowballTokenFilter()
                    .setLanguage(SnowballTokenFilterLanguage.ENGLISH)
                    .setName(generateName()),
                new StemmerOverrideTokenFilter()
                    .setRules(Collections.singletonList("ran => run"))
                    .setName(generateName()),
                new StemmerTokenFilter()
                    .setLanguage(StemmerTokenFilterLanguage.FRENCH)
                    .setName(generateName()),
                new StopwordsTokenFilter()
                    .setStopwords(Arrays.asList("a", "the"))
                    .setIgnoreCase(true)
                    .setRemoveTrailingStopWords(false)
                    .setName(generateName()),
                new StopwordsTokenFilter()
                    .setStopwordsList(StopwordsList.ITALIAN)
                    .setIgnoreCase(true)
                    .setRemoveTrailingStopWords(false)
                    .setName(generateName()),
                new SynonymTokenFilter()
                    .setSynonyms(Collections.singletonList("great, good"))
                    .setIgnoreCase(true)
                    .setExpand(false)
                    .setName(generateName()),
                new TruncateTokenFilter()
                    .setLength(10)
                    .setName(generateName()),
                new UniqueTokenFilter()
                    .setOnlyOnSamePosition(true)
                    .setName(generateName()),
                new UniqueTokenFilter()
                    .setName(generateName()),
                new WordDelimiterTokenFilter()
                    .setGenerateWordParts(false)
                    .setGenerateNumberParts(false)
                    .setCatenateWords(true)
                    .setCatenateNumbers(true)
                    .setCatenateAll(true)
                    .setSplitOnCaseChange(false)
                    .setPreserveOriginal(true)
                    .setSplitOnNumerics(false)
                    .setStemEnglishPossessive(false)
                    .setProtectedWords(Collections.singletonList("protected"))
                    .setName(generateName())
            ))
            .setCharFilters(Arrays.asList(
                new MappingCharFilter()
                    .setMappings(Collections.singletonList("a => b")) // One custom char filter for CustomeAnalyer above.
                    .setName(customCharFilterName.toString()),
                new MappingCharFilter()
                    .setMappings(Arrays.asList("s => $", "S => $"))
                    .setName(generateName()),
                new PatternReplaceCharFilter()
                    .setPattern("abc")
                    .setReplacement("123")
                    .setName(generateName())
            ));
    }

    Index createIndexWithSpecialDefaults() {
        int i = 0;

        return createTestIndex()
            .setAnalyzers(Arrays.asList(
                new PatternAnalyzer()
                    .setName(generateSimpleName(i++)),
                new StandardAnalyzer()
                    .setName(generateSimpleName(i++))
            ))
            .setTokenizers(Arrays.asList(
                new EdgeNGramTokenizer()
                    .setName(generateSimpleName(i++)),
                new NGramTokenizer()
                    .setName(generateSimpleName(i++)),
                new ClassicTokenizer()
                    .setName(generateSimpleName(i++)),
                new KeywordTokenizerV2()
                    .setName(generateSimpleName(i++)),
                new MicrosoftLanguageStemmingTokenizer()
                    .setName(generateSimpleName(i++)),
                new MicrosoftLanguageTokenizer()
                    .setName(generateSimpleName(i++)),
                new PathHierarchyTokenizerV2()
                    .setName(generateSimpleName(i++)),
                new PatternTokenizer()
                    .setName(generateSimpleName(i++)),
                new StandardTokenizerV2()
                    .setName(generateSimpleName(i++)),
                new UaxUrlEmailTokenizer()
                    .setName(generateSimpleName(i++))
            ))
            .setTokenFilters(Arrays.asList(
                new DictionaryDecompounderTokenFilter()
                    .setWordList(Collections.singletonList("Bahnhof"))
                    .setName(generateSimpleName(i++)),
                new EdgeNGramTokenFilterV2()
                    .setName(generateSimpleName(i++)),
                new LengthTokenFilter()
                    .setName(generateSimpleName(i++)),
                new LimitTokenFilter()
                    .setName(generateSimpleName(i++)),
                new NGramTokenFilterV2()
                    .setName(generateSimpleName(i++)),
                new PatternCaptureTokenFilter()
                    .setPatterns(Collections.singletonList("[a-z]*"))
                    .setName(generateSimpleName(i++)),
                new PhoneticTokenFilter()
                    .setName(generateSimpleName(i++)),
                new ShingleTokenFilter()
                    .setName(generateSimpleName(i++)),
                new StopwordsTokenFilter()
                    .setName(generateSimpleName(i++)),
                new SynonymTokenFilter()
                    .setSynonyms(Collections.singletonList("mutt, canine => dog"))
                    .setName(generateSimpleName(i++)),
                new TruncateTokenFilter()
                    .setName(generateSimpleName(i++)),
                new WordDelimiterTokenFilter()
                    .setName(generateSimpleName(i))
            ));
    }

    Index createExpectedIndexWithSpecialDefaults(Index index) {
        int i = 0;

        return createTestIndex()
            .setName(index.getName())
            .setAnalyzers(Arrays.asList(
                new PatternAnalyzer()
                    .setLowerCaseTerms(true)
                    .setPattern("\\W+")
                    .setName(generateSimpleName(i++)),
                new StandardAnalyzer()
                    .setMaxTokenLength(255)
                    .setName(generateSimpleName(i++))
            ))
            .setTokenizers(Arrays.asList(
                new EdgeNGramTokenizer()
                    .setMinGram(1)
                    .setMaxGram(2)
                    .setName(generateSimpleName(i++)),
                new NGramTokenizer()
                    .setMinGram(1)
                    .setMaxGram(2)
                    .setName(generateSimpleName(i++)),
                new ClassicTokenizer()
                    .setMaxTokenLength(255)
                    .setName(generateSimpleName(i++)),
                new KeywordTokenizerV2()
                    .setMaxTokenLength(256)
                    .setName(generateSimpleName(i++)),
                new MicrosoftLanguageStemmingTokenizer()
                    .setMaxTokenLength(255)
                    .setIsSearchTokenizer(false)
                    .setLanguage(MicrosoftStemmingTokenizerLanguage.ENGLISH)
                    .setName(generateSimpleName(i++)),
                new MicrosoftLanguageTokenizer()
                    .setMaxTokenLength(255)
                    .setIsSearchTokenizer(false)
                    .setLanguage(MicrosoftTokenizerLanguage.ENGLISH)
                    .setName(generateSimpleName(i++)),
                new PathHierarchyTokenizerV2()
                    .setDelimiter("/")
                    .setReplacement("/")
                    .setMaxTokenLength(300)
                    .setName(generateSimpleName(i++)),
                new PatternTokenizer()
                    .setPattern("\\W+")
                    .setGroup(-1)
                    .setName(generateSimpleName(i++)),
                new StandardTokenizerV2()
                    .setMaxTokenLength(255)
                    .setName(generateSimpleName(i++)),
                new UaxUrlEmailTokenizer()
                    .setMaxTokenLength(255)
                    .setName(generateSimpleName(i++))
            ))
            .setTokenFilters(Arrays.asList(
                new DictionaryDecompounderTokenFilter()
                    .setWordList(Collections.singletonList("Bahnhof"))
                    .setMinWordSize(5)
                    .setMinSubwordSize(2)
                    .setMaxSubwordSize(15)
                    .setName(generateSimpleName(i++)),
                new EdgeNGramTokenFilterV2()
                    .setMinGram(1)
                    .setMaxGram(2)
                    .setSide(EdgeNGramTokenFilterSide.FRONT)
                    .setName(generateSimpleName(i++)),
                new LengthTokenFilter()
                    .setMax(300)
                    .setName(generateSimpleName(i++)),
                new LimitTokenFilter()
                    .setMaxTokenCount(1)
                    .setName(generateSimpleName(i++)),
                new NGramTokenFilterV2()
                    .setMinGram(1)
                    .setMaxGram(2)
                    .setName(generateSimpleName(i++)),
                new PatternCaptureTokenFilter()
                    .setPatterns(Collections.singletonList("[a-z]*"))
                    .setPreserveOriginal(true)
                    .setName(generateSimpleName(i++)),
                new PhoneticTokenFilter()
                    .setEncoder(PhoneticEncoder.METAPHONE)
                    .setReplaceOriginalTokens(true)
                    .setName(generateSimpleName(i++)),
                new ShingleTokenFilter()
                    .setMaxShingleSize(2)
                    .setMinShingleSize(2)
                    .setOutputUnigrams(true)
                    .setTokenSeparator(" ")
                    .setFilterToken("_")
                    .setName(generateSimpleName(i++)),
                new StopwordsTokenFilter()
                    .setStopwordsList(StopwordsList.ENGLISH)
                    .setRemoveTrailingStopWords(true)
                    .setName(generateSimpleName(i++)),
                new SynonymTokenFilter()
                    .setExpand(true)
                    .setSynonyms(Collections.singletonList("mutt, canine => dog"))
                    .setName(generateSimpleName(i++)),
                new TruncateTokenFilter()
                    .setLength(300)
                    .setName(generateSimpleName(i++)),
                new WordDelimiterTokenFilter()
                    .setGenerateWordParts(true)
                    .setGenerateNumberParts(true)
                    .setSplitOnCaseChange(true)
                    .setSplitOnNumerics(true)
                    .setStemEnglishPossessive(true)
                    .setName(generateSimpleName(i))
            ));
    }

    void assertTokenInfoEqual(String expectedToken, Integer expectedStartOffset, Integer expectedEndOffset,
        Integer expectedPosition, TokenInfo actual) {
        assertEquals(expectedToken, actual.getToken());
        assertEquals(expectedStartOffset, actual.getStartOffset());
        assertEquals(expectedEndOffset, actual.getEndOffset());
        assertEquals(expectedPosition, actual.getPosition());
    }

    private String generateSimpleName(int n) {
        return String.format("a%d", n);
    }

    private List<AnalyzerName> getAnalyzersAllowedForSearchAnalyzerAndIndexAnalyzer() {
        // Only non-language analyzer names can be set on the searchAnalyzer and indexAnalyzer properties.
        // ASSUMPTION: Only language analyzers end in .lucene or .microsoft.
        return AnalyzerName.values()
            .stream()
            .filter(an -> !an.toString().endsWith(".lucene") && !an.toString().endsWith(".microsoft"))
            .collect(Collectors.toList());
    }
}
