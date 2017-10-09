package org.skywalking.apm.collector.cluster.zookeeper;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.skywalking.apm.collector.client.zookeeper.ZookeeperClient;
import org.skywalking.apm.collector.client.zookeeper.ZookeeperClientException;
import org.skywalking.apm.collector.client.zookeeper.util.PathUtils;
import org.skywalking.apm.collector.cluster.ClusterNodeExistException;
import org.skywalking.apm.collector.core.CollectorException;
import org.skywalking.apm.collector.core.client.Client;
import org.skywalking.apm.collector.core.client.ClientException;
import org.skywalking.apm.collector.core.client.DataMonitor;
import org.skywalking.apm.collector.core.cluster.ClusterDataListener;
import org.skywalking.apm.collector.core.module.ModuleRegistration;
import org.skywalking.apm.collector.core.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pengys5
 */
public class ClusterZKDataMonitor implements DataMonitor, Watcher {

    private final Logger logger = LoggerFactory.getLogger(ClusterZKDataMonitor.class);

    private ZookeeperClient client;

    private Map<String, ClusterDataListener> listeners;
    private Map<String, ModuleRegistration> registrations;

    public ClusterZKDataMonitor() {
        listeners = new LinkedHashMap<>();
        registrations = new LinkedHashMap<>();
    }

    @Override public synchronized void process(WatchedEvent event) {
        logger.info("changed path {}, event type: {}", event.getPath(), event.getType().name());
        if (listeners.containsKey(event.getPath())) {
            List<String> paths;
            try {
                paths = client.getChildren(event.getPath(), true);
                ClusterDataListener listener = listeners.get(event.getPath());
                Set<String> remoteNodes = new HashSet<>();
                Set<String> notifiedNodes = listener.getAddresses();
                if (CollectionUtils.isNotEmpty(paths)) {
                    for (String serverPath : paths) {
                        Stat stat = new Stat();
                        byte[] data = client.getData(event.getPath() + "/" + serverPath, true, stat);
                        String dataStr = new String(data);
                        String addressValue = serverPath + dataStr;
                        remoteNodes.add(addressValue);
                        if (!notifiedNodes.contains(addressValue)) {
                            logger.info("path children has been created, path: {}, data: {}", event.getPath() + "/" + serverPath, dataStr);
                            listener.addAddress(addressValue);
                            listener.serverJoinNotify(addressValue);
                        }
                    }
                }

                String[] notifiedNodeArray = notifiedNodes.toArray(new String[notifiedNodes.size()]);
                for (int i = notifiedNodeArray.length - 1; i >= 0; i--) {
                    String address = notifiedNodeArray[i];
                    if (remoteNodes.isEmpty() || !remoteNodes.contains(address)) {
                        logger.info("path children has been remove, path and data: {}", event.getPath() + "/" + address);
                        listener.removeAddress(address);
                        listener.serverQuitNotify(address);
                    }
                }
            } catch (ZookeeperClientException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override public void setClient(Client client) {
        this.client = (ZookeeperClient)client;
    }

    @Override public void start() throws CollectorException {
        Iterator<Map.Entry<String, ModuleRegistration>> entryIterator = registrations.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, ModuleRegistration> next = entryIterator.next();
            createPath(next.getKey());

            ModuleRegistration.Value value = next.getValue().buildValue();
            String contextPath = value.getContextPath() == null ? "" : value.getContextPath();

            client.getChildren(next.getKey(), true);
            String serverPath = next.getKey() + "/" + value.getHostPort();

            if (client.exists(serverPath, false) == null) {
                setData(serverPath, contextPath);
            } else {
                throw new ClusterNodeExistException("current address: " + value.getHostPort() + " has been registered, check the host and port configuration or wait a moment.");
            }
        }
    }

    @Override
    public void addListener(ClusterDataListener listener, ModuleRegistration registration) throws ClientException {
        String path = PathUtils.convertKey2Path(listener.path());
        logger.info("listener path: {}", path);
        listeners.put(path, listener);
        registrations.put(path, registration);
    }

    @Override public ClusterDataListener getListener(String path) {
        path = PathUtils.convertKey2Path(path);
        return listeners.get(path);
    }

    @Override public void createPath(String path) throws ClientException {
        String[] paths = path.replaceFirst("/", "").split("/");

        StringBuilder pathBuilder = new StringBuilder();
        for (String subPath : paths) {
            pathBuilder.append("/").append(subPath);
            if (client.exists(pathBuilder.toString(), false) == null) {
                client.create(pathBuilder.toString(), null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        }
    }

    @Override public void setData(String path, String value) throws ClientException {
        if (client.exists(path, false) == null) {
            client.create(path, value.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } else {
            client.setData(path, value.getBytes(), -1);
        }
    }
}
