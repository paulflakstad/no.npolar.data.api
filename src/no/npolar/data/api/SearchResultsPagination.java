package no.npolar.data.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import no.npolar.data.api.util.APIUtil;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Search results navigation: pagination – previous / page numbers / next.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute <flakstad at npolar.no>
 */
public class SearchResultsPagination {
    private int totalResults = -1;
    private int itemsPerPage = -1;
    private int startIndex = -1;
    private int pageNumber = -1;
    private int pagesTotal = -1;
    
    private String baseUri = null;
    private String parameterAppender = null;
    private String next = null;
    private String prev = null;
    
    private Map<String, String[]> serviceParamsFree = null;
    private Map<String, String[]> serviceParamsLocked = null;
    
    public static final String DEFAULT_CLASS_PAGINATION = "pagination";
    public static final String DEFAULT_CLASS_PAGINATION_INFO = DEFAULT_CLASS_PAGINATION + "__info";
    public static final String DEFAULT_CLASS_TRUNCATION = DEFAULT_CLASS_PAGINATION + "__truncation";
    public static final String DEFAULT_CLASS_PAGE = DEFAULT_CLASS_PAGINATION + "__page";
    public static final String DEFAULT_CLASS_PAGE_WRAPPER = DEFAULT_CLASS_PAGE + "-wrap";
    public static final String DEFAULT_CLASS_PAGE_PREV = APIUtil.classMod(DEFAULT_CLASS_PAGE, "prev") + " prev";
    //public static final String DEFAULT_CLASS_PAGE_PREV = DEFAULT_CLASS_PAGE + " " + DEFAULT_CLASS_PAGE + "--prev" + " prev";
    public static final String DEFAULT_CLASS_PAGE_NEXT = APIUtil.classMod(DEFAULT_CLASS_PAGE, "next") + " next";
    //public static final String DEFAULT_CLASS_PAGE_NEXT = DEFAULT_CLASS_PAGE + " " + DEFAULT_CLASS_PAGE + "--next" + " next";
    public static final String DEFAULT_CLASS_PAGE_NUMBERED = APIUtil.classMod(DEFAULT_CLASS_PAGE, "numbered");
    //public static final String DEFAULT_CLASS_PAGE_NUMBERED = DEFAULT_CLASS_PAGE + " " + DEFAULT_CLASS_PAGE + "--numbered";
    public static final String DEFAULT_CLASS_PAGE_CURRENT = APIUtil.classMod(DEFAULT_CLASS_PAGE, "numbered", "current") + " currentpage";
    //public static final String DEFAULT_CLASS_PAGE_CURRENT = DEFAULT_CLASS_PAGE_NUMBERED + " " + DEFAULT_CLASS_PAGE + "--current" + " currentpage";
    public static final String DEFAULT_CLASS_INACTIVE = "inactive";
    public static final String DEFAULT_CLASS_PAGE_PREV_WRAPPER = APIUtil.classMod(DEFAULT_CLASS_PAGE_WRAPPER, "prev") + " pagePrevWrap";
    //public static final String DEFAULT_CLASS_PAGE_PREV_WRAPPER = DEFAULT_CLASS_PAGE_WRAPPER + " " + DEFAULT_CLASS_PAGE_WRAPPER + "--prev" + " pagePrevWrap";
    public static final String DEFAULT_CLASS_PAGE_NEXT_WRAPPER = APIUtil.classMod(DEFAULT_CLASS_PAGE_WRAPPER, "next") + " pageNextWrap";
    //public static final String DEFAULT_CLASS_PAGE_NEXT_WRAPPER = DEFAULT_CLASS_PAGE_WRAPPER + " " + DEFAULT_CLASS_PAGE_WRAPPER + "--next" + " pageNextWrap";
    public static final String DEFAULT_CLASS_PAGES_NUMBERED_WRAPPER = APIUtil.classMod(DEFAULT_CLASS_PAGE_WRAPPER, "numbers") + " pageNumWrap";
    //public static final String DEFAULT_CLASS_PAGES_NUMBERED_WRAPPER = DEFAULT_CLASS_PAGE_WRAPPER + " " + DEFAULT_CLASS_PAGE_WRAPPER + "--numbers"+ " pageNumWrap";
    
    /**
     * Creates a new pagination, based on info provided by the given 
     * {@link APIService} instance, and using the base URI as a foundation for 
     * all pagination links.
     * <p>
     * The base URI is typically the "current" URI.
     * 
     * @param service The {@link APIService} instance to base the pagination on.
     * @param baseUri The URI to use as foundation for all pagination links, typically the "current" URI.
     */
    public SearchResultsPagination(APIService service, String baseUri) {
        totalResults = service.getTotalResults();
        itemsPerPage = service.getItemsPerPage();
        startIndex = service.getStartIndex();
        this.baseUri = baseUri;
        parameterAppender = baseUri.contains("?") ? "&amp;" : "?";
        try {
            serviceParamsFree = service.getParameters();
            serviceParamsLocked = service.getPresetParameters();
            pageNumber = (startIndex + itemsPerPage) / itemsPerPage;
            pagesTotal = (int)(Math.ceil((double)(totalResults + itemsPerPage) / itemsPerPage)) - 1;

            next = service.getNextPageParameters();
            prev = service.getPrevPageParameters();
        } catch (Exception e) {
            // Ignore
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
     * @param uri The link target (<code>href</code> attribute), properly escaped.
     * @param missingUriClass The class to apply to the link in case the URI is empty or <code>null</code>.
     * @return The ready-to-use link.
     */
    protected String constructLink(String clazz, String uri, String missingUriClass) {
        String link = "<a class=\"" + clazz;
        if (uri == null || uri.isEmpty()) {
            link += " " + missingUriClass;
        }
        else {
            link += "\" href=\"" + uri;
        }
        link += "\"></a>"; // ToDo: Add text here (not accessible as of now) => requires some localization...
        return link;
    }
    
    public String appendToBase(String parameterString) {
        return baseUri + parameterAppender + StringEscapeUtils.escapeHtml(parameterString);
    }
    
    public List<String> inBetweenPages() {
        List<String> pages = new ArrayList<String>();
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
                            + appendToBase(
                                    APIUtil.toParameterString(serviceParamsFree, APIService.Param.START_AT)
                                    + "&" + APIService.Param.START_AT + "=" + ((pageCounter-1) * itemsPerPage)
                            ) + "\">" + pageCounter + "</a>");
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
        return pages;
    }
    
    /**
     * Gets the "standard" pagination HTML.
     * 
     * @return  the "standard" pagination HTML.
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
