package org.vsegda;

import org.vsegda.data.MessageSession;
import org.vsegda.data.MessageItem;
import org.vsegda.data.MessageQueue;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.jdo.JDOObjectNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.*;

/**
 * @author Roman Elizarov
 */
public class MessageServlet extends HttpServlet {
    private static final String SESSION = "session";
    private static final String NEW_SESSION = "newsession";

    @SuppressWarnings({"unchecked"})
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        writeGetResponse(resp, new MessageRequest(req));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MessageRequest messageRequest = new MessageRequest(req);
        long now = System.currentTimeMillis();
        Map<Long, Long> sessions = parseSessions(req);
        Map<Long, List<MessageItem>> items = parseMessageItems(req.getReader(), now);
        Set<Long> queues = new HashSet<Long>(sessions.keySet());
        queues.addAll(items.keySet());
        for (Long queue : queues) {
            long sessionId = postToQueue(queue, sessions.get(queue), items.get(queue), now);
            sessions.put(queue, sessionId);
        }
        addSessionCookies(resp, sessions);
        // combine poll for messages with POST
        if (messageRequest.isPollRequest())
            writeGetResponse(resp, messageRequest);
    }

    private void writeGetResponse(HttpServletResponse resp, MessageRequest messageRequest) throws IOException {
        resp.setContentType("text/csv");
        resp.setCharacterEncoding("UTF-8");
        ServletOutputStream out = resp.getOutputStream();
        PersistenceManager pm = Factory.getPersistenceManager();
        try {
            for (MessageItem item : messageRequest.query(pm)) {
                out.println(item.toString());
            }
        } finally {
            pm.close();
        }
    }

    private long postToQueue(long queueId, Long sessionId, List<MessageItem> items, long now) {
        PersistenceManager pm = Factory.getPersistenceManager();
        try {
            Transaction tx = pm.currentTransaction();
            try {
                tx.begin();
                MessageQueue queue = getOrCreateMessageQueue(pm, queueId);
                MessageSession session = getOrCreateMessageSession(pm, queue, sessionId, now);
                long maxSessionPostIndex = session.getLastPostIndex();
                if (items != null)
                    for (MessageItem item : items) {
                        if (item.getMessageIndex() <= session.getLastPostIndex())
                            continue; // was already posted
                        maxSessionPostIndex = Math.max(maxSessionPostIndex, session.getLastPostIndex());
                        long index = queue.getLastPostIndex() + 1;
                        queue.setLastPostIndex(index);
                        item.setMessageIndex(index);
                        pm.makePersistent(item);
                    }
                session.setLastPostIndex(maxSessionPostIndex);
                tx.commit();
                return session.getSessionId();
            } finally {
                if (tx.isActive())
                    tx.rollback();
            }
        } finally {
            pm.close();
        }
    }

    private MessageQueue getOrCreateMessageQueue(PersistenceManager pm, long queueId) {
        try {
            return pm.getObjectById(MessageQueue.class, MessageQueue.createKey(queueId));
        } catch (JDOObjectNotFoundException e) {
            return pm.makePersistent(new MessageQueue(MessageQueue.createKey(queueId)));
        }
    }

    private MessageSession getOrCreateMessageSession(PersistenceManager pm, MessageQueue queue, Long sessionId, long now) {
        if (sessionId != null)
            return pm.getObjectById(MessageSession.class, MessageSession.createKey(queue.getQueueId(), sessionId));
        sessionId = queue.getLastSessionId() + 1;
        queue.setLastSessionId(sessionId);
        return pm.makePersistent(new MessageSession(queue.getQueueId(), sessionId, now));
    }

    @SuppressWarnings({"unchecked"})
    private Map<Long, Long> parseSessions(HttpServletRequest req) {
        Map<Long, Long> sessions = new HashMap<Long, Long>();
        // get sessions from cookies
        Cookie[] cookies = req.getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (name.startsWith(SESSION)) {
                    long queueId = Long.parseLong(name.substring(SESSION.length()));
                    long sessionId = Long.parseLong(cookie.getValue());
                    sessions.put(queueId, sessionId);
                }
            }
        // get sessions from parameters
        for (Map.Entry<String, String> entry : ((Map<String, String>)req.getParameterMap()).entrySet()) {
            String name = entry.getKey();
            if (name.startsWith(SESSION)) {
                long queueId = Long.parseLong(name.substring(SESSION.length()));
                long sessionId = Long.parseLong(entry.getValue());
                sessions.put(queueId, sessionId);
            }
            if (name.startsWith(NEW_SESSION)) {
                long queueId = Long.parseLong(name.substring(NEW_SESSION.length()));
                sessions.put(queueId, null);
            }
        }
        return sessions;
    }

    private void addSessionCookies(HttpServletResponse resp, Map<Long, Long> sessions) {
        for (Map.Entry<Long, Long> entry : sessions.entrySet()) {
            resp.addCookie(new Cookie(SESSION + entry.getKey(), "" + entry.getValue()));
        }
    }

    private Map<Long, List<MessageItem>> parseMessageItems(BufferedReader in, long now) throws IOException {
        String line;
        Map<Long, List<MessageItem>> items = new HashMap<Long, List<MessageItem>>();
        while ((line = in.readLine()) != null) {
            MessageItem item = new MessageItem(line, now);
            List<MessageItem> list = items.get(item.getQueueId());
            if (list == null)
                items.put(item.getQueueId(), list = new ArrayList<MessageItem>());
            list.add(item);
        }
        return items;
    }
}
