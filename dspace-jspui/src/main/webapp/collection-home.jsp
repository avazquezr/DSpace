<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

--%>
<%--
  - Collection home JSP
  -
  - Attributes required:
  -    collection  - Collection to render home page for
  -    community   - Community this collection is in
  -    last.submitted.titles - String[], titles of recent submissions
  -    last.submitted.urls   - String[], corresponding URLs
  -    logged.in  - Boolean, true if a user is logged in
  -    subscribed - Boolean, true if user is subscribed to this collection
  -    admin_button - Boolean, show admin 'edit' button
  -    editor_button - Boolean, show collection editor (edit submitters, item mapping) buttons
  -    show.items - Boolean, show item list
  -    browse.info - BrowseInfo, item list
  --%>

<%@ page contentType="text/html;charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@page import="org.dspace.app.webui.servlet.MyDSpaceServlet"%>
<%@ page import="org.dspace.app.webui.components.RecentSubmissions" %>
<%@page import="org.dspace.discovery.IGlobalSearchResult"%>
<%@ page import="org.dspace.app.webui.servlet.admin.EditCommunitiesServlet" %>
<%@ page import="org.dspace.app.webui.util.UIUtil" %>
<%@ page import="org.dspace.browse.BrowseIndex" %>
<%@ page import="org.dspace.browse.BrowseInfo" %>
<%@ page import="org.dspace.browse.ItemCounter"%>
<%@ page import="org.dspace.content.*"%>
<%@ page import="org.dspace.core.ConfigurationManager"%>
<%@ page import="org.dspace.core.Context" %>
<%@ page import="org.dspace.core.Utils" %>
<%@ page import="org.dspace.eperson.Group"     %>
<%@ page import="javax.servlet.jsp.jstl.fmt.LocaleSupport" %>
<%@ page import="java.net.URLEncoder" %>

<%
    // Retrieve attributes
    Collection collection = (Collection) request.getAttribute("collection");
    Community  community  = (Community) request.getAttribute("community");
    Group      submitters = (Group) request.getAttribute("submitters");

    RecentSubmissions rs = (RecentSubmissions) request.getAttribute("recently.submitted");
    
    boolean loggedIn =
        ((Boolean) request.getAttribute("logged.in")).booleanValue();
    boolean subscribed =
        ((Boolean) request.getAttribute("subscribed")).booleanValue();
    Boolean admin_b = (Boolean)request.getAttribute("admin_button");
    boolean admin_button = (admin_b == null ? false : admin_b.booleanValue());

    Boolean editor_b      = (Boolean)request.getAttribute("editor_button");
    boolean editor_button = (editor_b == null ? false : editor_b.booleanValue());

    Boolean submit_b      = (Boolean)request.getAttribute("can_submit_button");
    boolean submit_button = (submit_b == null ? false : submit_b.booleanValue());

	// get the browse indices
    BrowseIndex[] bis = BrowseIndex.getBrowseCollectionIndices();

    // Put the metadata values into guaranteed non-null variables
    String name = collection.getMetadata("name");
    String intro = collection.getMetadata("introductory_text");
    if (intro == null)
    {
        intro = "";
    }
    String copyright = collection.getMetadata("copyright_text");
    if (copyright == null)
    {
        copyright = "";
    }
    String sidebar = collection.getMetadata("side_bar_text");
    if(sidebar == null)
    {
        sidebar = "";
    }

    String communityName = community.getMetadata("name");
    String communityLink = "/handle/" + community.getHandle();

    Bitstream logo = collection.getLogo();
    
    boolean feedEnabled = ConfigurationManager.getBooleanProperty("webui.feed.enable");
    String feedData = "NONE";
    if (feedEnabled)
    {
        feedData = "coll:" + ConfigurationManager.getProperty("webui.feed.formats");
    }
    
    ItemCounter ic = new ItemCounter(UIUtil.obtainContext(request));

    Boolean showItems = (Boolean)request.getAttribute("show.items");
    boolean show_items = showItems != null ? showItems.booleanValue() : false;
    
    String formaction = request.getContextPath() + "/handle/" + collection.getHandle();
%>
<script type="text/javascript">
function sortBy(idx, ord)
{
       jQuery("#ssort_by").val(idx);
       jQuery("#sorder").val(ord);
       jQuery("#sortform").submit();
}
</script>
<dspace:layout locbar="commLink" title="<%= name %>" feedData="<%= feedData %>">
    <div class="well">
    <div class="row">
     <%  if (logo != null) { %>
        <div class="col-md-8">
     <% } else {%>
        <div class="col-md-12">
     <% } %>
     <h2><%= name %>
<%
            if(ConfigurationManager.getBooleanProperty("webui.strengths.show"))
            {
%>
                : [<%= ic.getCount(collection) %>]
<%
            }
%>
		<small><fmt:message key="jsp.collection-home.heading1"/></small>
      <a class="statisticsLink btn btn-info" href="<%= request.getContextPath() %>/cris/stats/collection.html?handle=<%= collection.getHandle() %>&type=selected"><fmt:message key="jsp.collection-home.display-statistics"/></a>
      </h2></div>
<%  if (logo != null) { %>
        <div class="col-md-4">
        	<img class="img-responsive pull-right" alt="Logo" src="<%= request.getContextPath() %>/retrieve/<%= logo.getID() %>" />
        </div>
<% 	} %>
	</div>
<%
	if (StringUtils.isNotBlank(intro)) { %>
	<%= intro %>
<% 	} %>
  </div>
  <p class="copyrightText"><%= copyright %></p>
  
  <%-- Browse --%>
  <div class="panel panel-primary">
  	<div class="panel-heading">
        <fmt:message key="jsp.general.browse"/>
	</div>
	<div class="panel-body">
	<%-- Insert the dynamic list of browse options --%>
<%
	for (int i = 0; i < bis.length; i++)
	{
		String key = "browse.menu." + bis[i].getName();
%>
	<form method="get" class="btn-group" action="<%= request.getContextPath() %>/handle/<%= collection.getHandle() %>/browse">
		<input type="hidden" name="type" value="<%= bis[i].getName() %>"/>
		<%-- <input type="hidden" name="collection" value="<%= collection.getHandle() %>" /> --%>
		<input type="submit" class="btn btn-default" name="submit_browse" value="<fmt:message key="<%= key %>"/>"/>
	</form>
<%	
	}
%>	</div>
</div>
<%  if (submit_button)
    { %>
          <form class="form-group" action="<%= request.getContextPath() %>/submit" method="post">
            <input type="hidden" name="collection" value="<%= collection.getID() %>" />
			<input class="btn btn-success col-md-12" type="submit" name="submit" value="<fmt:message key="jsp.collection-home.submit.button"/>" />
          </form>
<%  } %>
        <form class="well" method="get" action="">
<%  if (loggedIn && subscribed)
    { %>
                <small><fmt:message key="jsp.collection-home.subscribed"/> <a href="<%= request.getContextPath() %>/subscribe"><fmt:message key="jsp.collection-home.info"/></a></small>
           		<input class="btn btn-sm btn-warning" type="submit" name="submit_unsubscribe" value="<fmt:message key="jsp.collection-home.unsub"/>" />
<%  } else { %>
                <small>
            		  <fmt:message key="jsp.collection-home.subscribe.msg"/>
                </small>
				<input class="btn btn-sm btn-info" type="submit" name="submit_subscribe" value="<fmt:message key="jsp.collection-home.subscribe"/>" />
<%  }
    if(feedEnabled)
    { %>
    <span class="pull-right">
    <%
    	String[] fmts = feedData.substring(5).split(",");
    	String icon = null;
    	int width = 0;
    	for (int j = 0; j < fmts.length; j++)
    	{
    		if ("rss_1.0".equals(fmts[j]))
    		{
    		   icon = "rss1.gif";
    		   width = 80;
    		}
    		else if ("rss_2.0".equals(fmts[j]))
    		{
    		   icon = "rss2.gif";
    		   width = 80;
    		}
    		else
    	    {
    	       icon = "rss.gif";
    	       width = 36;
    	    }
%>
    <a href="<%= request.getContextPath() %>/feed/<%= fmts[j] %>/<%= collection.getHandle() %>"><img src="<%= request.getContextPath() %>/image/<%= icon %>" alt="RSS Feed" width="<%= width %>" height="15" style="margin: 3px 0 3px" /></a>
<%
    	} %>
    	</span><%
    }
%>
        </form>

<div class="row">
	<%@ include file="discovery/static-tagcloud-facet.jsp" %>
</div>

<% if (show_items)
   {
        BrowseInfo bi = (BrowseInfo) request.getAttribute("browse.info");
        BrowseIndex bix = bi.getBrowseIndex();
        String direction = (bi.isAscending() ? "ASC" : "DESC");
        String sortBy = ((String)request.getParameter("sort_by"))==null?"-1":request.getParameter("sort_by");
        // prepare the next and previous links
        String linkBase = request.getContextPath() + "/handle/" + collection.getHandle();
        
        String next = linkBase;
        String prev = linkBase;
        
        if (bi.hasNextPage())
        {
            next = next + "?offset=" + bi.getNextOffset() + "&sort_by=" + sortBy + "&order=" + direction;
        }
        
        if (bi.hasPrevPage())
        {
            prev = prev + "?offset=" + bi.getPrevOffset() + "&sort_by=" + sortBy + "&order=" + direction;
        }
        
        String bi_name_key = "browse.menu." + bi.getSortOption().getName();
        String so_name_key = "browse.order." + (bi.isAscending() ? "asc" : "desc");
%>
    <%-- give us the top report on what we are looking at --%>
    <fmt:message var="bi_name" key="<%= bi_name_key %>"/>
    <fmt:message var="so_name" key="<%= so_name_key %>"/>
    <div class="browse_range">
        <fmt:message key="jsp.collection-home.content.range">
            <fmt:param value="${bi_name}"/>
            <fmt:param value="${so_name}"/>
            <fmt:param value="<%= Integer.toString(bi.getStart()) %>"/>
            <fmt:param value="<%= Integer.toString(bi.getFinish()) %>"/>
            <fmt:param value="<%= Integer.toString(bi.getTotal()) %>"/>
        </fmt:message>
    </div>

    <%--  do the top previous and next page links --%>
    <div class="prev-next-links">
<% 
      if (bi.hasPrevPage())
      {
%>
      <a href="<%= prev %>"><fmt:message key="browse.full.prev"/></a>&nbsp;
<%
      }

      if (bi.hasNextPage())
      {
%>
      &nbsp;<a href="<%= next %>"><fmt:message key="browse.full.next"/></a>
<%
      }
%>
    </div>

<%-- output the results using the browselist tag --%>
<%
      if (bix.isMetadataIndex())
      {
%>
      <dspace:browselist browseInfo="<%= bi %>" emphcolumn="<%= bix.getMetadata() %>"/>
<%
      }
      else
      {
%>
      <dspace:browselist browseInfo="<%= bi %>" emphcolumn="<%= bix.getSortOption().getMetadata() %>"/>
<%
      }
%>

    <%-- give us the bottom report on what we are looking at --%>
    <div class="browse_range">
        <fmt:message key="jsp.collection-home.content.range">
            <fmt:param value="${bi_name}"/>
            <fmt:param value="${so_name}"/>
            <fmt:param value="<%= Integer.toString(bi.getStart()) %>"/>
            <fmt:param value="<%= Integer.toString(bi.getFinish()) %>"/>
            <fmt:param value="<%= Integer.toString(bi.getTotal()) %>"/>
        </fmt:message>
    </div>

    <%--  do the bottom previous and next page links --%>
    <div class="prev-next-links">
<% 
      if (bi.hasPrevPage())
      {
%>
      <a href="<%= prev %>"><fmt:message key="browse.full.prev"/></a>&nbsp;
<%
      }

      if (bi.hasNextPage())
      {
%>
      &nbsp;<a href="<%= next %>"><fmt:message key="browse.full.next"/></a>
<%
      }
%>
    </div>
   <form class="form-inline hidden"  id="sortform" method="get" action="<%= formaction %>">
<%
                if (bi.hasAuthority())
                {
                %><input type="hidden" name="authority" value="<%=bi.getAuthority() %>"/><%
                }
                else if (bi.hasValue())
                {
                        %><input type="hidden" name="value" value="<%= bi.getValue() %>"/><%
                }
%>
                <input type="hidden" id="ssort_by" name="sort_by" value="" />
                <input type="hidden" id="sorder" name="order" value="<%= direction %>" />
                <input type="hidden" id="offset" name="offset" value="<%= request.getParameter("offset")==null?0:request.getParameter("offset") %>" />
		</form>
<%
   } // end of if (show_title)
%>

  <dspace:sidebar>
<% if(admin_button || editor_button ) { %>
                 <div class="panel panel-warning">
                 <div class="panel-heading"><fmt:message key="jsp.admintools"/>
                 	<span class="pull-right"><dspace:popup page="<%= LocaleSupport.getLocalizedMessage(pageContext, \"help.collection-admin\")%>"><fmt:message key="jsp.adminhelp"/></dspace:popup></span>
                 </div>
                 <div class="panel-body">              
<% if( editor_button ) { %>
                <form method="post" action="<%=request.getContextPath()%>/tools/edit-communities">
                  <input type="hidden" name="collection_id" value="<%= collection.getID() %>" />
                  <input type="hidden" name="community_id" value="<%= community.getID() %>" />
                  <input type="hidden" name="action" value="<%= EditCommunitiesServlet.START_EDIT_COLLECTION %>" />
                  <input class="btn btn-default col-md-12" type="submit" value="<fmt:message key="jsp.general.edit.button"/>" />
                </form>
<% } %>

<% if( admin_button ) { %>
                 <form method="post" action="<%=request.getContextPath()%>/tools/itemmap">
                  <input type="hidden" name="cid" value="<%= collection.getID() %>" />
				  <input class="btn btn-default col-md-12" type="submit" value="<fmt:message key="jsp.collection-home.item.button"/>" />                  
                </form>
<% if(submitters != null) { %>
		      <form method="get" action="<%=request.getContextPath()%>/tools/group-edit">
		        <input type="hidden" name="group_id" value="<%=submitters.getID()%>" />
		        <input class="btn btn-default col-md-12" type="submit" name="submit_edit" value="<fmt:message key="jsp.collection-home.editsub.button"/>" />
		      </form>
<% } %>
<% if( editor_button || admin_button) { %>
                <form method="post" action="<%=request.getContextPath()%>/mydspace">
                  <input type="hidden" name="collection_id" value="<%= collection.getID() %>" />
                  <input type="hidden" name="step" value="<%= MyDSpaceServlet.REQUEST_EXPORT_ARCHIVE %>" />
                  <input class="btn btn-default col-md-12" type="submit" value="<fmt:message key="jsp.mydspace.request.export.collection"/>" />
                </form>
               <form method="post" action="<%=request.getContextPath()%>/mydspace">
                 <input type="hidden" name="collection_id" value="<%= collection.getID() %>" />
                 <input type="hidden" name="step" value="<%= MyDSpaceServlet.REQUEST_MIGRATE_ARCHIVE %>" />
                 <input class="btn btn-default col-md-12" type="submit" value="<fmt:message key="jsp.mydspace.request.export.migratecollection"/>" />
               </form>
               <form method="post" action="<%=request.getContextPath()%>/dspace-admin/metadataexport">
                 <input type="hidden" name="handle" value="<%= collection.getHandle() %>" />
                 <input class="btn btn-default col-md-12" type="submit" value="<fmt:message key="jsp.general.metadataexport.button"/>" />
               </form>
               </div>
               </div>
<% } %>
                 
<% } %>

<%  } %>

<%
	if (rs != null)
	{
%>
	<h3><fmt:message key="jsp.collection-home.recentsub"/></h3>
<%
		for (IGlobalSearchResult obj : rs.getRecentSubmissions()) {
		%>
		
				<dspace:discovery-artifact style="global" artifact="<%= obj %>" view="<%= rs.getConfiguration() %>"/>
		
		<%
		}
%>
    <p>&nbsp;</p>
<%      } %>

    <%= sidebar %>
    <%
    	int discovery_panel_cols = 12;
    	int discovery_facet_cols = 12;
    	String processorSidebar = (String) request.getAttribute("processorSidebar");
    
    if(processorSidebar!=null && processorSidebar.equals("sidebar")) {
	%>
	<%@ include file="discovery/static-sidebar-facet.jsp" %>
	<% } %>	
  </dspace:sidebar>

</dspace:layout>

