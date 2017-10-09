package org.skywalking.apm.collector.ui.jetty.handler;

import com.google.gson.JsonElement;
import javax.servlet.http.HttpServletRequest;
import org.skywalking.apm.collector.core.util.StringUtils;
import org.skywalking.apm.collector.server.jetty.ArgumentsParseException;
import org.skywalking.apm.collector.server.jetty.JettyHandler;
import org.skywalking.apm.collector.ui.dao.ISegmentCostDAO;
import org.skywalking.apm.collector.ui.service.SegmentTopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pengys5
 */
public class SegmentTopGetHandler extends JettyHandler {

    private final Logger logger = LoggerFactory.getLogger(SegmentTopGetHandler.class);

    @Override public String pathSpec() {
        return "/segment/top";
    }

    private SegmentTopService service = new SegmentTopService();

    @Override protected JsonElement doGet(HttpServletRequest req) throws ArgumentsParseException {
        if (!req.getParameterMap().containsKey("startTime") || !req.getParameterMap().containsKey("endTime") || !req.getParameterMap().containsKey("from") || !req.getParameterMap().containsKey("limit")) {
            throw new ArgumentsParseException("the request parameter must contains startTime, endTime, from, limit");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("startTime: {}, endTime: {}, from: {}", req.getParameter("startTime"), req.getParameter("endTime"), req.getParameter("from"));
        }

        long startTime;
        try {
            startTime = Long.valueOf(req.getParameter("startTime"));
        } catch (NumberFormatException e) {
            throw new ArgumentsParseException("the request parameter startTime must be a long");
        }

        long endTime;
        try {
            endTime = Long.valueOf(req.getParameter("endTime"));
        } catch (NumberFormatException e) {
            throw new ArgumentsParseException("the request parameter endTime must be a long");
        }

        int from;
        try {
            from = Integer.valueOf(req.getParameter("from"));
        } catch (NumberFormatException e) {
            throw new ArgumentsParseException("the request parameter from must be an integer");
        }

        int limit;
        try {
            limit = Integer.valueOf(req.getParameter("limit"));
        } catch (NumberFormatException e) {
            throw new ArgumentsParseException("the request parameter from must be an integer");
        }

        int minCost = -1;
        if (req.getParameterMap().containsKey("minCost")) {
            minCost = Integer.valueOf(req.getParameter("minCost"));
        }
        int maxCost = -1;
        if (req.getParameterMap().containsKey("maxCost")) {
            maxCost = Integer.valueOf(req.getParameter("maxCost"));
        }

        String globalTraceId = null;
        if (req.getParameterMap().containsKey("globalTraceId")) {
            globalTraceId = req.getParameter("globalTraceId");
        }

        String operationName = null;
        if (req.getParameterMap().containsKey("operationName")) {
            operationName = req.getParameter("operationName");
        }

        int applicationId;
        try {
            applicationId = Integer.valueOf(req.getParameter("applicationId"));
        } catch (NumberFormatException e) {
            throw new ArgumentsParseException("the request parameter applicationId must be a int");
        }

        ISegmentCostDAO.Error error;
        String errorStr = req.getParameter("error");
        if (StringUtils.isNotEmpty(errorStr)) {
            if ("true".equals(errorStr)) {
                error = ISegmentCostDAO.Error.True;
            } else if ("false".equals(errorStr)) {
                error = ISegmentCostDAO.Error.False;
            } else {
                error = ISegmentCostDAO.Error.All;
            }
        } else {
            error = ISegmentCostDAO.Error.All;
        }

        ISegmentCostDAO.Sort sort = ISegmentCostDAO.Sort.Cost;
        if (req.getParameterMap().containsKey("sort")) {
            String sortStr = req.getParameter("sort");
            if (sortStr.toLowerCase().equals(ISegmentCostDAO.Sort.Time.name().toLowerCase())) {
                sort = ISegmentCostDAO.Sort.Time;
            }
        }

        return service.loadTop(startTime, endTime, minCost, maxCost, operationName, globalTraceId, error, applicationId, limit, from, sort);
    }

    @Override protected JsonElement doPost(HttpServletRequest req) throws ArgumentsParseException {
        throw new UnsupportedOperationException();
    }
}
