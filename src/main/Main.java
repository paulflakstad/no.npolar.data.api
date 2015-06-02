package main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import no.npolar.data.api.*;
import no.npolar.data.api.mosj.*;
import no.npolar.data.api.util.*;
import static no.npolar.data.api.util.APIUtil.httpResponseAsString;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.util.CmsHtmlExtractor;

/**
 * Tester.
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class Main {
    public static void p(String s) {
        System.out.print(s);
    }
    public static void pl(String s) {
        System.out.println(s);
        /*
        // Write to html file as well, so we can view in browser
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("D:/pubs.html", false)));
            out.println("<html><head><title>Main</title><style>html{font-family:monospace,sans-serif;} ul li {padding:0.5em 0; border-bottom:1px solid #ddd;} .pub-contributor-npi { font-weight:bold;}</style></head><body>");
            out.println(s);
            out.println("</body></html>");
            out.close();
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }*/
        
    }
    
    public static void main(String[] args) {
        if (true) {
            /*
            try {
                JSONObject serviceResponseObject = new JSONObject(httpResponseAsString("http://api.npolar.no/indicator/timeseries/0efb37ac-6a3b-487d-b3bf-c9f5f340aa08"));
            } catch (java.io.FileNotFoundException missingFileException) {
                pl("No API entry with that ID.");
            } catch (Exception e) {    
                e.printStackTrace();
            }
            //*/
            
            /*
            //
            // Testing retrieve single publication
            //
            PublicationService service = new PublicationService(new Locale("en"));
            service.setDefaultParameters(new HashMap<String, String[]>());
            try {
                //Publication p = service.getPublication("9338632e-a6a1-4242-83a8-35ae17c99c52");
                //Publication p = service.getPublication("8e379f15-25f7-446b-bfe3-2f0796de89ab");
                Publication p = service.getPublication("3daebc24-f98e-4aab-96e7-83213104c324");
                if (p != null)
                    System.out.println(p.toString());
                else
                    System.out.println("No such publication");
            } catch (Exception e) {
                e.printStackTrace();
            }
            //*/
            
            //*
            //
            // Testing MOSJ API 3
            //
            Locale loc = new Locale("no");
            ResourceBundle labels = ResourceBundle.getBundle(no.npolar.data.api.Labels.getBundleName(), loc);

            //MOSJService service = new MOSJTestService(loc, false);
            MOSJService service = new MOSJService(loc, true);

            try {
                
                //String id = "2a4e646a-509f-5bf7-b478-333a722670f3";
                //String id = "a6e2c395-9e40-5703-af8b-be1ac1a456f6";
                //String id ="93a13173-f0ea-5514-9764-0d3f6f4b04dd";
                //String id = "44c4f706-5007-5c8e-b421-db1903c223a5"; // Kvikksølv (Hg) i egg fra polarlomvi
                //String id = "2a4e646a-509f-5bf7-b478-333a722670f3"; // Stabile organiske miljøgifter (POPs) i polartorsk (multiple time series)
                //String id = "214efdf2-ed37-5df9-b26c-277cb229a2df"; // Lufttemp. og nedbør
                //String id = "b10e3b9f-83eb-5d6d-be0c-b94f62e05d5f"; // Antall personer gått i land [...]
                //String id = "752751da-ee9c-5a07-8128-ede08300a9bb";
                //String id = "02f947ce-715b-58c0-a2bb-5a7998516611";
                //String id = "88c2dd73-3128-50f9-b36f-91d0cc397e14"; // Kondisjon hos voksne isbjørnhanner
                //String id = "29ac5f80-8dc8-5b1a-8adf-cac862178ba8"; // Cesium-137 i torsk
                //String id = "84d22a6e-87a2-5c6a-8b6d-ca3a92661c7e"; // PCB i luft
                //String id = "bf8a48fb-13b2-572e-8ea6-4e39039564b6"; // UV-doser
                //String id = "e4609ad3-c852-5489-9500-31b90ff4498d"; // Uttak: isbjørn
                //String id ="fdbafa00-844b-57db-8cc7-dbe27f2d41e0"; // Havisutbredelse i Barentshavet i april
                String id = "1bfab4ea-109e-5cc4-bf2e-2d133d49b565";
                
                pl("Getting MOSJ parameter with ID " + id + " ...");
                
                // Get the MOSJ parameter
                MOSJParameter mp = service.getMOSJParameter(id).setDisplayLocale(loc);
                //mp.setDisplayLocale(loc);
                
                //mp.getTitle()
                
                // Create override object
                //JSONObject overrides = new JSONObject();
                JSONObject overrides = new JSONObject("{\"series\":[{\"id\":\"c4d6d6d5-fcc3-465e-9151-7116525ddc79\",\"trendLine\":\"true\"},{\"id\":\"bdda8689-820d-4288-a948-e97f09b83964\",\"lineThickness\":\"0\"},{\"id\":\"b093b268-ae43-485a-a49d-3918ba1a8c0d\",\"lineThickness\":\"0\"},{\"id\":\"9e93e67d-e744-4217-9421-1e4181466db1\",\"trendLine\":\"true\",\"color\":\"#eee\"}]}");
                //JSONObject overrides = new JSONObject("{\"series\":[{\"id\":\"c4d6d6d5-fcc3-465e-9151-7116525ddc79\",\"trendLine\":\"true\"},{\"id\":\"bdda8689-820d-4288-a948-e97f09b83964\",\"lineThickness\":\"0\"},{\"id\":\"b093b268-ae43-485a-a49d-3918ba1a8c0d\",\"lineThickness\":\"0\"},{\"id\":\"9e93e67d-e744-4217-9421-1e4181466db1\",\"trendLine\":\"true\"},{\"id\":\"9e93e67d-e744-4217-9421-1e4181466db1\",\"color\":\"#eee\"}]}");
                //JSONObject overrides = new JSONObject("{\"series\":[" 
                //                                        + "{ \"id\":\"9e93e67d-e744-4217-9421-1e4181466db1\", \"trendLine\":\"true\" }" 
                                                        //+ "{ \"id\":\"9e93e67d-e744-4217-9421-1e4181466db1\", \"trendLine\":\"true\", \"color\":\"#eee\" }" 
                                                        //+ "{\"id\":\"4bef90cf-2d79-45fb-a9a7-64abda0002e0\", \"dots\":\"false\", \"trendLine\":\"true\"}" 
                                                        //+ ",{id:'d4487834-31b2-4838-8f76-59b15c81eacb', dots:'false', trendLine:'true'}" 
                                                        //+ ",{id:'077d780d-19de-4040-b474-9614a592301f', dots:'false', trendLine:'true'}" 
                                                        //+ ",{id:'b6563b5f-d2d7-49e5-9bfc-f5db8211b4be', dots:'false', trendLine:'true'}"
                //                                    + "]}");
                //JSONObject overrides = new JSONObject("{ series: [{id:'a74c550f-25b3-526d-a5c2-92945db572c4', type:'bar'}] }");
                //JSONObject overrides = new JSONObject("{ errorToggler: true, type: column, series: [{id:'a74c550f-25b3-526d-a5c2-92945db572c4', type:'bar'}] }");
                //JSONObject overrides = new JSONObject("{ series: [{id:'94225197-ad05-5c19-9c9d-c1e8bfeaf1f9', type:'column', yAxis:'1'}, {id:'1af3f195-1c25-5130-b1f5-3b9ce1ffcbd2', yAxis:'0'}, {id:'14bf4a87-8966-52a2-a86e-7695bda9deb9', yAxis:'0'}] }");

                pl("Parameter: " + mp.getTitle(loc) + " -- " + mp.getURL(service));

                List<TimeSeries> tss = mp.getTimeSeries();
                if (tss != null && !tss.isEmpty()) {
                    pl("Related time series:");
                    Iterator<TimeSeries> i = tss.iterator(); 
                    while (i.hasNext()) {
                        TimeSeries ts = i.next();
                        pl(""
                                //+ "\n" + ts.getAsTable()
                                //+ "    " + ts.getTitle(loc)
                                + "    " + ts.getLabel()
                                + " -- " + ts.getURL(service)
                                //+ " " + ts.getId()
                                //+ " [[" + ts.getUnitVerbose(loc) + "]]"
                                //+ " [[" + APIUtil.listToString(ts.getDataPoints(loc), null, ", ") + "]]"
                                //+ APIUtil.getStringByLocale(ts.getAPIStructure().getJSONArray("titles"), "title", loc)
                                //+ ts.getAPIStructure().getJSONArray("titles").getJSONObject(0).getString("title")
                                );
                        
                        //pl(ts.getAPIStructure().getJSONArray("titles").toString(4));
                        //break;
                    }
                }
                
                HighchartsChart chart = mp.getChart(overrides);
                //pl("\n############\nTable format:\n" + chart.getHtmlTable());
                pl("\n############\nTable format:\n" + mp.getAsTable("responsive"));
                pl("\n############\nStandard format:\n" + chart.getChartConfigurationString());
                pl("\n############\nCSV format:\n" + mp.getAsCSV());
                //
                //pl(mp.getHighchartsConfig(overrides));

                //pl("Collected " + tss.size() + " related timeseries.");
            } catch (Exception e) {
                pl("Error: " + e.getMessage());
                e.printStackTrace();
            }
            //*/
            
            //*/
            /*
            //
            // Testing MOSJ API 2
            //
            Locale loc = new Locale("no");
            ResourceBundle labels = ResourceBundle.getBundle(Labels.getBundleName(), loc);

            MOSJService service = new MOSJService(loc);
            HashMap<String, String[]> params = new HashMap<String, String[]>();
            params.put("q"                  , new String[]{ "" }); // Catch-all query
            //params.put("filter-topics"      , new String[]{ "biology,climate" }); // SearchFilter by a theme identifier
            //params.put("filter-topics"      , new String[]{ "biology" }); // SearchFilter by a theme identifier
            //params.put("sort"               , new String[]{ "-published-year" }); // Sort by publish year, descending
            params.put("limit"              , new String[]{ "1" }); // Limit the results
            params.put("q-id"               , new String[] { "a6e2c395-9e40-5703-af8b-be1ac1a456f6" }); // Fetch this ID
            params.put("facets"             , new String[] { "false" });

            // Duplicating the service class' own fallback default parameters, just to test
            //service.addDefaultParameter("filter-draft", new String[]{ "no" });
            //service.addDefaultParameter("facets", new String[]{ "publication_type,topics,category,programme" });
            //service.addDefaultParameter("sort", new String[]{ "-published-year" });

            try {
                MOSJParameter mp = service.getMOSJParameter("a6e2c395-9e40-5703-af8b-be1ac1a456f6");
                pl("Parameter: " + mp.getTitle(loc));
                List<MOSJTimeSeries> tss = mp.getTimeSeries();
                if (tss != null && !tss.isEmpty()) {
                    Iterator<MOSJTimeSeries> i = tss.iterator(); 
                    while (i.hasNext()) {
                        MOSJTimeSeries ts = i.next();
                        pl("\nTimeseries:" 
                                + "\n" + ts.getAsTable()
                                //+ " " + ts.getID() 
                                //+ " " + ts.getTitle(loc) 
                                //+ " [[" + ts.getUnitVerbose(loc) + "]]"
                                //+ " [[" + APIUtil.listToString(ts.getDataPoints(loc), null, ", ") + "]]"
                                );
                                //+ APIUtil.getStringByLocale(ts.getAPIStructure().getJSONArray("titles"), "title", loc));
                                //+ ts.getAPIStructure().getJSONArray("titles").getJSONObject(0).getString("title"));
                    }
                }
                //pl("Collected " + tss.size() + " related timeseries.");
            } catch (Exception e) {
                pl(" =0   E R R O R !   =O ");
                e.printStackTrace();
            }
            //*/
            /*
            //
            // Testing MOSJ API 1
            //
            Locale loc = new Locale("no");
            ResourceBundle labels = ResourceBundle.getBundle(Labels.getBundleName(), loc);

            MOSJService service = new MOSJService(loc);
            HashMap<String, String[]> params = new HashMap<String, String[]>();
            params.put("q"                  , new String[]{ "" }); // Catch-all query
            //params.put("filter-topics"      , new String[]{ "biology,climate" }); // SearchFilter by a theme identifier
            //params.put("filter-topics"      , new String[]{ "biology" }); // SearchFilter by a theme identifier
            //params.put("sort"               , new String[]{ "-published-year" }); // Sort by publish year, descending
            params.put("limit"              , new String[]{ "1" }); // Limit the results
            params.put("q-id"               , new String[] { "a6e2c395-9e40-5703-af8b-be1ac1a456f6" }); // Fetch this ID
            params.put("facets"             , new String[] { "false" });

            // Duplicating the service class' own fallback default parameters, just to test
            //service.addDefaultParameter("filter-draft", new String[]{ "no" });
            //service.addDefaultParameter("facets", new String[]{ "publication_type,topics,category,programme" });
            //service.addDefaultParameter("sort", new String[]{ "-published-year" });

            try {
                List<MOSJParameter> list = service.getMOSJParameters(params);
                SearchFilterSets filterSets = service.getFilterSets();

                filterSets.order(new String[] { "topics", "publication_type" } );

                pl("Collected " + list.size() + " parameters - service URL: " + service.getLastServiceURL() + "");

                if (list == null || list.isEmpty()) {
                    pl("No hits");
                } else if (list.size() > 1) {
                    pl("Multiple hits.");                
                } else {

                    String pid = null;

                    Iterator<MOSJParameter> itr = list.iterator();
                    while (itr.hasNext()) {
                        MOSJParameter mp = itr.next();
                        pl(mp.getTitle(loc) + " - " + mp.getID());
                        pid = mp.getID();
                    }

                    List<MOSJTimeseries> tss = service.getMOSJTimeseriesForParameter(pid);
                    pl("Collected " + tss.size() + " related timeseries.");
                }
                //pl("Prev page: " + service.getPrevPageParameters());
                //pl("Next page: " + service.getNextPageParameters());
                //pl("-Prev page: " + service.getPrevPageFullUrl());
                //pl("-Next page: " + service.getNextPageFullUrl());
            } catch (Exception e) {
                pl(" =0   E R R O R !   =O ");
                e.printStackTrace();
            }
            //*/
            /*
            //
            // Person-based projects test
            //
            ProjectService service = new ProjectService(null);
            HashMap<String, String[]> params = new HashMap<String, String[]>();
            params.put("q"                  , new String[]{ "" }); // Catch-all query
            params.put("filter-people.email", new String[]{ "kit.kovacs@npolar.no" }); // SearchFilter by this person's identifier
            params.put("sort"               , new String[]{ "-start_date" }); // Sort by publish year, descending
            params.put("limit"              , new String[]{ "all" }); // Limit the results

            Mapper m = new Mapper();
            m.addMapping(Project.STATE_NAME_ACTIVE, "Active");
            m.addMapping(Project.STATE_NAME_COMPLETED, "Completed");
            m.addMapping(Project.STATE_NAME_PLANNED, "Planned");
            m.addMapping(Project.STATE_NAME_UNDEFINED, "Undefined");

            try {
                GroupedCollection<Project> projects = service.getProjects(params);
                pl("Collected " + projects.size() + " projects - service URL: " + service.getLastServiceURL() + "");

                Iterator<String> iGroups = projects.getTypesContained().iterator();
                while (iGroups.hasNext()) {
                    String groupName = iGroups.next();
                    List<Project> list = projects.getListGroup(groupName);
                    if (!list.isEmpty()) {
                        pl("\n=== " + m.getMapping(groupName).toUpperCase() + " (" + list.size() + ") ===");
                        Iterator<Project> i = list.iterator();
                        while (i.hasNext()) {
                            Project proj = i.next();

                            pl(proj.getTitle() + "."
                                    + "\n\tDuration: " + proj.getDuration(false) 
                                    + "\n\tParticipants: " + proj.getParticipantsStr(false)
                                    + "\n\tLeader(s): " + proj.getLeadersStr(false)
                                    + "\n\tType(s): " + proj.getType() 
                                    + "\n\tTopic(s): " + proj.getTopics() 
                                    + "\n\tLink: http://data.npolar.no/project/" + proj.getId() 

                                    //+ " (" + m.getMapping(proj.getStateName()) + ")"
                                    );
                        }
                    }
                }
            } catch (Exception e) {
                pl(" =0   E R R O R !   =O ");
                //e.printStackTrace();
            }
            //*/

            /*
            //
            // Person-based publications test
            //
            Locale loc = new Locale("en");
            PublicationService service = new PublicationService(loc);
            HashMap<String, String[]> params = new HashMap<String, String[]>();
            params.put("q"                  , new String[]{ "" }); // Catch-all query
            //params.put("filter-people.email", new String[]{ "christina.pedersen@npolar.no" }); // SearchFilter by this person's identifier
            //params.put("filter-people.email", new String[]{ "winfried.dallmann@npolar.no" }); // SearchFilter by this person's identifier
            //params.put("filter-people.email", new String[]{ "sebastian.gerland@npolar.no" }); // SearchFilter by this person's identifier
            //params.put("filter-people.email", new String[]{ "pedro.duarte@npolar.no" }); // SearchFilter by this person's identifier
            //params.put("filter-people.email", new String[]{ "helle.goldman@npolar.no" }); // SearchFilter by this person's identifier
            params.put("filter-people.email", new String[]{ "gunnar.sander@npolar.no" }); // SearchFilter by this person's identifier
            //params.put("filter-people.email", new String[]{ "jan.gunnar.winther@npolar.no" }); // SearchFilter by this person's identifier
            //params.put("sort"               , new String[]{ "-published-year" }); // Sort by publish year, descending
            params.put("sort"               , new String[]{ "-published_sort" }); // Sort by publish time, descending
            params.put("limit"              , new String[]{ "all" }); // Limit the results

            Mapper m = new Mapper();

            // Localization test, using resource bundle
            ResourceBundle labels = ResourceBundle.getBundle(Labels.getBundleName(), loc);
            m.addAllPipeSeparated(labels.getString(Labels.DATA_COUNTRIES_0));

            String s = "";

            boolean html = false;

            try {
                GroupedCollection<Publication> coll = service.getPublications(params);
                if (html)
                    s += "<p>";
                s += "Collected " + coll.size() + " publications using service URL: \n" + service.getLastServiceURL() + "";
                if (html)
                    s+= "</p>";
                s += "\n";


                Iterator<String> iGroups = coll.getTypesContained().iterator();
                while (iGroups.hasNext()) {
                    String groupName = iGroups.next();
                    List<Publication> list = coll.getListGroup(groupName);
                    if (!list.isEmpty()) {
                        s += "\n";
                        if (html)
                            s += "<h3>";
                        s += "=== " + labels.getString("publication.type." + groupName + (list.size() > 1 ? ".plural" : "")).toUpperCase() + " (" + list.size() + ") ===";
                        if (html)
                            s += "</h3>";
                        //s += "\n";
                        if (html)
                            s += "<ul>";
                        s += "\n";
                        Iterator<Publication> i = list.iterator();
                        while (i.hasNext()) {
                            Publication item = i.next();

                            List<PublicationContributor> people = item.getPeople();

                            if (html)
                                s += "<li>";
                            else
                                s += "\n";

                            s += html ? item.toString() : CmsHtmlExtractor.extractText(item.toString(), "utf-8");

                            if (html)
                                s+= "</li>";

                            s += "\n";
                            //s += "(parent: " + item.hasParent() + " / url:" + item.getParentUrl() + " / ID:" + item.getParentId() + ")\n";
                        }
                        if (html)
                            s += "</ul>";
                        s += "\n\n";
                    }                
                }
                pl(s);
            } catch (Exception e) {
                pl(" =0   E R R O R !   =O ");
                e.printStackTrace();
            }
            //*/

            /*
            //
            // Theme-based publications test
            //
            Locale loc = new Locale("no");
            ResourceBundle labels = ResourceBundle.getBundle(Labels.getBundleName(), loc);

            PublicationService service = new PublicationService(loc);
            HashMap<String, String[]> params = new HashMap<String, String[]>();
            params.put("q"                  , new String[]{ "" }); // Catch-all query
            params.put("filter-topics"      , new String[]{ "biology,climate" }); // SearchFilter by a theme identifier
            params.put("sort"               , new String[]{ "-published-year" }); // Sort by publish year, descending
            params.put("limit"              , new String[]{ "10" }); // Limit the results

            try {
                List<Publication> list = service.getPublicationList(params);
                pl("Collected " + list.size() + " publications - service URL: " + service.getLastServiceURL() + "");

                if (!list.isEmpty()) {
                    Iterator<Publication> i = list.iterator();
                    while (i.hasNext()) {
                        Publication item = i.next();
                        pl("[" + labels.getString(Labels.PUB_TYPE_PREFIX_0.concat(item.getType())) + "] " + item.toString() + "\nTags: " + item.getTopicsHtml("http://npolar.no/?lol", ",", loc) + "\n");
                    }
                }
            } catch (Exception e) {
                pl(" =0   E R R O R !   =O ");
                e.printStackTrace();
            }
            //*/

            /*
            //
            // Testing APIUtil's parameter methods
            //
            Map<String, List<String>> m = APIUtil.getQueryParametersFromString("http://api.npolar.no/publication/" 
                    + "?start=0"
                    + "&limit=10"
                    + "&facets=organisation,publication_year,topics,category,publisher,links.rel,links.href,people.email,draft"
                    + "&sort=-published-year"
                    + "&filter-published-year=2014"
                    + "&q=&format=json"
                    + "&filter-organisations.id=npolar.no"
                    );

            System.out.println(APIUtil.getParameterString(m) + "\n");
            Iterator<String> iKeys = m.keySet().iterator();
            while (iKeys.hasNext()) {
                String key = iKeys.next();
                System.out.println(key);
                List<String> vals = m.get(key);
                Iterator<String> iVals = vals.iterator();
                while (iVals.hasNext()) {
                    System.out.println("\t" + iVals.next());
                }
            }
            //*/

            /*
            //
            // Testing filters
            //
            Locale loc = new Locale("no");
            ResourceBundle labels = ResourceBundle.getBundle(Labels.getBundleName(), loc);

            PublicationService service = new PublicationService(loc);
            HashMap<String, String[]> params = new HashMap<String, String[]>();
            params.put("q"                  , new String[]{ "" }); // Catch-all query
            //params.put("filter-topics"      , new String[]{ "biology,climate" }); // SearchFilter by a theme identifier
            //params.put("filter-topics"      , new String[]{ "biology" }); // SearchFilter by a theme identifier
            //params.put("sort"               , new String[]{ "-published-year" }); // Sort by publish year, descending
            params.put("limit"              , new String[]{ "10" }); // Limit the results

            // Duplicating the service class' own fallback default parameters, just to test
            service.addDefaultParameter("filter-draft", new String[]{ "no" });
            service.addDefaultParameter("facets", new String[]{ "publication_type,topics,category,programme" });
            service.addDefaultParameter("sort", new String[]{ "-published-year" });

            try {
                List<Publication> list = service.getPublicationList(params);
                SearchFilterSets filterSets = service.getFilterSets();
                filterSets.order(new String[] { "topics", "publication_type" } );

                pl("Collected " + list.size() + " publications - service URL: " + service.getLastServiceURL() + "");

                if (filterSets == null || filterSets.isEmpty()) {
                    pl("No filters");
                } else {
                    pl("" + filterSets.size() + " filters:");
                    Iterator<SearchFilterSet> iFacets = filterSets.iterator();
                    while (iFacets.hasNext()) {
                        SearchFilterSet facet = iFacets.next();
                        pl(facet.getTitle(loc) + " - " + facet.getName() + " R=" + facet.getRelevancy() + " (" + facet.size() + "):");
                        Iterator<SearchFilter> iFilters = facet.getFilters().iterator();
                        while (iFilters.hasNext()) {
                            SearchFilter filter = iFilters.next();
                            String filterNiceName = facet.labelKeyFor(filter);
                            try { filterNiceName = labels.getString(filterNiceName); } catch (Exception e) {} // Error = no match for key
                            pl("\t" + (filter.isActive() ? "[ ON  ] " : "[ off ] ") 
                                    + " | " + filterNiceName
                                    + " | " + filter.getTerm() 
                                    + " | " + filter.getUrlPartParameters());
                        }
                    }
                }
                //pl("Prev page: " + service.getPrevPageParameters());
                //pl("Next page: " + service.getNextPageParameters());
                //pl("-Prev page: " + service.getPrevPageFullUrl());
                //pl("-Next page: " + service.getNextPageFullUrl());
            } catch (Exception e) {
                pl(" =0   E R R O R !   =O ");
                e.printStackTrace();
            }
            //*/
        
        }
    }
}
