package no.npolar.data.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import no.npolar.data.api.util.APIUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Search results navigation: pagination – previous / page numbers / next.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute <flakstad at npolar.no>
 */
public class SearchResultsPagination {
    /** Total results for the search. */
    private int totalResults = -1;
    /** Items per page for the search. */
    private int itemsPerPage = -1;
    /** Start index for the search. */
    private int startIndex = -1;
    /** Current page number for the search. */
    private int pageNumber = -1;
    /** Total number of pages for the search. */
    private int pagesTotal = -1;
    
    /** 
     * Base URI for the web page that displays the search results. 
     * <p>
     * It is named "base URI" because it should be empty of any service-related 
     * parameters, and ready to have search parameters appended.
     * <p>
     * So, for example:
     * <ul>
     * <li>/my/serp.html</li>
     * <li>/my/serp.html?foo=bar</li>
     * <li>NOT <del>/my/serp.html?q=foo&start=10</del></li>
     * </ul>
     */
    private String baseUri = null;
    /** The character used when appending parameters, either &amp; or ?. */
    private String parameterAppender = null;
    /** The set of parameters, as a parameter string, that can be used to get the next page. */
    private String next = null;
    /** The set of parameters, as a parameter string, that can be used to get the previous page. */
    private String prev = null;
    
    /** The service's freely modifiable parameters. */
    private Map<String, String[]> serviceParamsFree = null;
    /** The service's locked (non-modifiable) parameters. */
    private Map<String, String[]> serviceParamsLocked = null;
    
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(no.npolar.data.api.SearchResultsPagination.class);
    
    /** Class name for the entire pagination component. */
    public static final String DEFAULT_CLASS_PAGINATION = "pagination";
    /** Class name for the "info" element inside pagination. */
    public static final String DEFAULT_CLASS_PAGINATION_INFO = DEFAULT_CLASS_PAGINATION + "__info";
    /** Class name for the "truncated pages" element inside pagination. */
    public static final String DEFAULT_CLASS_TRUNCATION = DEFAULT_CLASS_PAGINATION + "__truncation";
    /** Class name for page elements inside pagination. */
    public static final String DEFAULT_CLASS_PAGE = DEFAULT_CLASS_PAGINATION + "__page";
    /** Class name for page wrapper elements inside pagination. */
    public static final String DEFAULT_CLASS_PAGE_WRAPPER = DEFAULT_CLASS_PAGE + "-wrap";
    /** Class name for the previous page element inside pagination. */
    public static final String DEFAULT_CLASS_PAGE_PREV = APIUtil.classMod(DEFAULT_CLASS_PAGE, "prev") + " prev";
    //public static final String DEFAULT_CLASS_PAGE_PREV = DEFAULT_CLASS_PAGE + " " + DEFAULT_CLASS_PAGE + "--prev" + " prev";
    /** Class name for the next page element inside pagination. */
    public static final String DEFAULT_CLASS_PAGE_NEXT = APIUtil.classMod(DEFAULT_CLASS_PAGE, "next") + " next";
    //public static final String DEFAULT_CLASS_PAGE_NEXT = DEFAULT_CLASS_PAGE + " " + DEFAULT_CLASS_PAGE + "--next" + " next";
    /** Class name for numbered page elements inside pagination. */
    public static final String DEFAULT_CLASS_PAGE_NUMBERED = APIUtil.classMod(DEFAULT_CLASS_PAGE, "numbered");
    //public static final String DEFAULT_CLASS_PAGE_NUMBERED = DEFAULT_CLASS_PAGE + " " + DEFAULT_CLASS_PAGE + "--numbered";
    /** Class name for the current page element inside pagination. */
    public static final String DEFAULT_CLASS_PAGE_CURRENT = APIUtil.classMod(DEFAULT_CLASS_PAGE, "numbered", "current") + " currentpage";
    //public static final String DEFAULT_CLASS_PAGE_CURRENT = DEFAULT_CLASS_PAGE_NUMBERED + " " + DEFAULT_CLASS_PAGE + "--current" + " currentpage";
    /** Class name for inactive elements inside pagination. */
    public static final String DEFAULT_CLASS_INACTIVE = "inactive";
    /** Class name for the previous page wrapper element inside pagination. */
    public static final String DEFAULT_CLASS_PAGE_PREV_WRAPPER = APIUtil.classMod(DEFAULT_CLASS_PAGE_WRAPPER, "prev") + " pagePrevWrap";
    //public static final String DEFAULT_CLASS_PAGE_PREV_WRAPPER = DEFAULT_CLASS_PAGE_WRAPPER + " " + DEFAULT_CLASS_PAGE_WRAPPER + "--prev" + " pagePrevWrap";
    /** Class name for the next page wrapper element inside pagination. */
    public static final String DEFAULT_CLASS_PAGE_NEXT_WRAPPER = APIUtil.classMod(DEFAULT_CLASS_PAGE_WRAPPER, "next") + " pageNextWrap";
    //public static final String DEFAULT_CLASS_PAGE_NEXT_WRAPPER = DEFAULT_CLASS_PAGE_WRAPPER + " " + DEFAULT_CLASS_PAGE_WRAPPER + "--next" + " pageNextWrap";
    /** Class name for the numbered pages wrapper element inside pagination. */
    public static final String DEFAULT_CLASS_PAGES_NUMBERED_WRAPPER = APIUtil.classMod(DEFAULT_CLASS_PAGE_WRAPPER, "numbers") + " pageNumWrap";
    //public static final String DEFAULT_CLASS_PAGES_NUMBERED_WRAPPER = DEFAULT_CLASS_PAGE_WRAPPER + " " + DEFAULT_CLASS_PAGE_WRAPPER + "--numbers"+ " pageNumWrap";
    
    /**
     * Creates a new pagination, based on info provided by the given 
     * {@link APIService} instance, and using the base URI as a foundation for 
     * all pagination links.
     * <p>
     * The base URI is typically the "current" URI, without any parameters. 
     * However, it should be safe to pass parameters as well, if necessary.
     * 
     * @param service The {@link APIService} instance to base the pagination on.
     * @param baseUri The URI to use as foundation for all pagination links, typically the "current" URI.
     */
    public SearchResultsPagination(APIService service, String baseUri) {
        totalResults = service.getTotalResults();
        itemsPerPage = service.getItemsPerPage();
        startIndex = service.getStartIndex();
        
        this.baseUri = baseUri;
        parameterAppender = baseUri.contains("?") ? "&" : "?";
        
        try {
            serviceParamsFree = service.getParameters();
            serviceParamsLocked = service.getPresetParameters();
            
            try {
                // Retain any "extra" parameters present in the base URI (that 
                // is, any parameter not related to the service)
                Map<String, String> baseUriParams = APIUtil.getParametersInQueryString(baseUri);
                if (!baseUriParams.isEmpty()) {
                    Iterator<String> iBaseUriParams = baseUriParams.keySet().iterator();
                    while (iBaseUriParams.hasNext()) {
                        String key = iBaseUriParams.next();
                        if (serviceParamsFree.containsKey(key) || serviceParamsLocked.containsKey(key)) {
                            iBaseUriParams.remove();
                        }
                    }
                    this.baseUri = baseUri.split("\\?")[0];
                    if (baseUriParams.isEmpty()) {
                        parameterAppender = "?";
                    } else {
                        this.baseUri += "?" + APIUtil.getParameterString(baseUriParams);
                        parameterAppender = "&";
                    }
                }
            } catch (Exception ee) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Error processing base URI with parameters, intended for pagination.", ee);
                }
            }
            pageNumber = (startIndex + itemsPerPage) / itemsPerPage;
            pagesTotal = (int)(Math.ceil((double)(totalResults + itemsPerPage) / itemsPerPage)) - 1;

            next = service.getNextPageParameters();
            prev = service.getPrevPageParameters();
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error creating pagination.", e);
            }
        }
    }
    
    
    /**
     * Gets the URI to the previous page, or null if there is none.
     * 
     * @return  the URI to the previous page, or null if there is none.
     */
    public String prevPageUri() {
        // Require at least one previous page, if we are to return a URI
        return pageNumber > 1 ? appendToBase(prev) : null;
    }
    
    /**
     * Gets the URI to the next page, or null if there is none.
     * 
     * @return the URI to the next page, or null if there is none.
     */
    public String nextPageUri() {
        // Require more than 1 page total, and at least one more page, if we are to return a URI
        return (pagesTotal > 1 && pageNumber < pagesTotal) ? appendToBase(next) : null;
    }
    
    /**
     * Gets the link to the previous page (may be inactive/disabled).
     * 
     * @param clazz The link class. If <code>null</code>, a standard class set is used.
     * @return the link to the "previous page" (may be inactive/disabled).
     */
    public String prevPage(String clazz) {
        return constructLink(clazz == null ? DEFAULT_CLASS_PAGE_PREV : clazz, prevPageUri(), DEFAULT_CLASS_INACTIVE);
    }

    /**
     * Gets the standard link to the previous page.
     * 
     * @return  a call to {@link #prevPage(java.lang.String)}, passing <code>null</code> as argument.
     * @see #prevPage(java.lang.String) 
     */
    public String prevPage() {
        return prevPage(null);
    }
    
    /**
     * Gets the link to the next page (may be inactive/disabled).
     * 
     * @param clazz The link class. If <code>null</code>, a standard class set is used.
     * @return the link to the "next page" (may be inactive/disabled).
     */
    public String nextPage(String clazz) {
        return constructLink(clazz == null ? DEFAULT_CLASS_PAGE_NEXT : clazz, nextPageUri(), DEFAULT_CLASS_INACTIVE);
    }
    
    /**
     * Gets the standard link to the next page.
     * 
     * @return a call to {@link #nextPage(java.lang.String)}, passing <code>null</code> as argument.
     * @see #nextPage(java.lang.String) 
     */
    public String nextPage() {
        return nextPage(null);
    }
    
    /**
     * Constructs a link to the given URI, with the given classes applied as
     * needed.
     * <p>
     * If the given URI is empty or <code>null</code>, the link will have no 
     * <code>href</code> attribute, and will have the given missingUriClass 
     * class applied. (This is needed by for example {@link #nextPageUri()}, 
     * which will return <code>null</code> if there is no next page.)
     * 
     * @param clazz The class to apply to the link.
     * @param uri The link target (<code>href</code> attribute), unescaped.
     * @param missingUriClass The class to apply to the link in case the URI is empty or <code>null</code>.
     * @return The ready-to-use link.
     */
    protected String constructLink(String clazz, String uri, String missingUriClass) {
        String link = "<a class=\"" + clazz;
        if (uri == null || uri.isEmpty()) {
            link += " " + missingUriClass;
        }
        else {
            link += "\" href=\"" + StringEscapeUtils.escapeHtml(uri);
        }
        link += "\"></a>"; // ToDo: Add text here (not accessible as of now) => requires some localization...
        return link;
    }
    
    /**
     * Appends the given parameter string to the base URI.
     * 
     * @param parameterString The parameter string to append.
     * @return The base URI, with the given parameter string appended.
     */
    public String appendToBase(String parameterString) {
        return baseUri + parameterAppender + parameterString;
    }
    
    /**
     * Gets the in-between pages - that is, the numbered page span in-between 
     * the "next" and "previous" page.
     * 
     * @return The in-between pages.
     */
    public List<String> inBetweenPages() {
        List<String> pages = new ArrayList<String>();
        try {
            for (int pageCounter = 1; pageCounter <= pagesTotal; pageCounter++) {
                boolean splitNav = pagesTotal >= 8;
                // if (first page OR last page OR (pages total > 10 AND (this page number > (current page number - 4) AND this page number < (current page number + 4)))
                // Pseudo: if this is the first page, the last page, or a page close to the current page (± 4)
                if (!splitNav
                            || (splitNav && (pageCounter == 1 || pageCounter == pagesTotal))
                            //|| (pagesTotal > 10 && (pageCounter > (pageNumber-4) && pageCounter < (pageNumber+4)))) {
                            || (splitNav && (pageCounter > (pageNumber-3) && pageCounter < (pageNumber+3)))) {
                    if (pageCounter != pageNumber) {
                        // Not the current page: print a link
                        pages.add("<a class=\"" + DEFAULT_CLASS_PAGE_NUMBERED + "\" href=\"" 
                                + StringEscapeUtils.escapeHtml(
                                        appendToBase(
                                            APIUtil.toParameterString(serviceParamsFree, APIService.Param.START_AT)
                                            + "&" + APIService.Param.START_AT + "=" + ((pageCounter-1) * itemsPerPage)
                                        )
                                )
                                + "\">" + pageCounter + "</a>");
                    }
                    else {
                        // The current page: no link
                        pages.add("<span class=\"" + DEFAULT_CLASS_PAGE_CURRENT + "\">" + pageCounter + "</span>");
                    }
                }
                // Pseudo: 
                else if (splitNav && (pageCounter == 2 || pageCounter+1 == pagesTotal)) { 
                    pages.add("<span class=\"" + DEFAULT_CLASS_TRUNCATION + "\"> &hellip; </span>");
                } else {
                    //pages.add("<!-- page " + pageCounter + " dropped ... -->");
                }
            }
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error creating the in-between pages for pagination.", e);
            }
        }
        return pages;
    }
    
    /**
     * Gets the default pagination HTML.
     * 
     * @return The default pagination HTML.
     */
    public String getPaginationHtml() {
        String s = "";
        if (totalResults > 0) {
            if (pagesTotal > 1) {
                s += "\n<nav class=\"" + DEFAULT_CLASS_PAGINATION + " clearfix\">";
                s += "\n<!--<span class=\"" + DEFAULT_CLASS_PAGINATION_INFO + "\">Page " + pageNumber + " of " + pagesTotal + "</span>-->";
                    //s += "\n<!--<span class=\"pagination__info\">Page " + pageNumber + " of " + pagesTotal + "</span>-->";
                    s += "\n<div class=\"" + DEFAULT_CLASS_PAGE_PREV_WRAPPER + "\">";
                    //s += "\n<div class=\"pagePrevWrap pagination__page-wrap pagination__page-wrap--prev\">";
                        s += prevPage();
                    s += "\n</div>";
                    s += "\n<div class=\"" + DEFAULT_CLASS_PAGES_NUMBERED_WRAPPER + "\">";
                    //s += "\n<div class=\"pageNumWrap pagination__page-wrap pagination__page-wrap--numbers\">";
                        for (String inBetweenLink : inBetweenPages()) {
                            s += inBetweenLink;
                        }
                    s += "\n</div>";
                    s += "\n<div class=\"" + DEFAULT_CLASS_PAGE_NEXT_WRAPPER + "\">";
                    //s += "\n<div class=\"pageNextWrap pagination__page-wrap pagination__page-wrap--next\">";
                        s += nextPage();
                    s += "\n</div>";
                s += "\n</nav>";
            }
        }
        return s;
    }
    
    /**
     * Sets a new base URI.
     * 
     * @param newBaseUri The new base URI.
     * @return This instance, updated.
     */
    public SearchResultsPagination setBaseUri(String newBaseUri) {
        baseUri = newBaseUri;
        return this;
    }
    
    
    /*
    public String getPaginationHtml() {
        String s = "";
        
        if (totalResults > 0) {
            if (pagesTotal > 1) {
                s += "\n<nav class=\"pagination clearfix\">";
                    s += "\n<div class=\"pagePrevWrap\">";

                        if (pagesTotal > 1) { // More than one page total
                            s += "\n<a class=\"prev";
                            if (pageNumber > 1) { // At least one previous page exists
                                s += "\" href=\"" + prevPageUri();
                            }
                            else { // No previous page
                                s += " inactive"; // Add "inactive" class
                            }
                            s += "\"></a>"; // ToDo: Add text here (not accessible as of now) => requires some localization...
                        }
                    s += "\n</div>";
                    s += "\n<div class=\"pageNumWrap\">";
                        
                        for (int pageCounter = 1; pageCounter <= pagesTotal; pageCounter++) {
                            boolean splitNav = pagesTotal >= 8;
                            // if (first page OR last page OR (pages total > 10 AND (this page number > (current page number - 4) AND this page number < (current page number + 4)))
                            // Pseudo: if this is the first page, the last page, or a page close to the current page (± 4)
                            if (!splitNav
                                        || (splitNav && (pageCounter == 1 || pageCounter == pagesTotal))
                                        //|| (pagesTotal > 10 && (pageCounter > (pageNumber-4) && pageCounter < (pageNumber+4)))) {
                                        || (splitNav && (pageCounter > (pageNumber-3) && pageCounter < (pageNumber+3)))) {
                                if (pageCounter != pageNumber) { // Not the current page: print a link
                                    s += "\n<a href=\"" + baseUri
                                            + "?" 
                                            + StringEscapeUtils.escapeHtml(APIUtil.toParameterString(serviceParamsFree, APIService.Param.START_AT) 
                                            + "&" + APIService.Param.START_AT + "=" + ((pageCounter-1) * itemsPerPage)) + "\">"
                                        + pageCounter +
                                    "</a>";
                                }
                                else { // The current page: no link
                                    s += "\n<span class=\"currentpage\">" + pageCounter + "</span>";
                                }
                            }
                            // Pseudo: 
                            else if (splitNav && (pageCounter == 2 || pageCounter+1 == pagesTotal)) { 
                                s += "\n<span> &hellip; </span>";
                            } else {
                                //s += "<!-- page " + pageCounter + " dropped ... -->";
                            }
                        }
                        
                    s += "\n</div>";
                    s += "\n<div class=\"pageNextWrap\">";
                        s += "\n<!--<span>Page " + pageNumber + " of " + pagesTotal + "</span>-->";

                        if (pagesTotal > 1) { // More than one page total
                            s += "\n<a class=\"next";
                            if (pageNumber < pagesTotal) { // At least one more page exists
                                s += "\" href=\"" + nextPageUri();
                            }
                            else {
                                s += " inactive";
                            }
                            s += "\"></a>";
                        }
                    s += "\n</div>";
                s += "\n</nav>";
            }
        }
        return s;
    }*/
}
