package com.guigarage.platform.sample.data;

import com.canoo.platform.remoting.server.event.RemotingEventBus;
import com.canoo.platform.remoting.server.event.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static com.guigarage.platform.sample.data.Topics.CLEAR;
import static com.guigarage.platform.sample.data.Topics.NEW_ITEMS;
import static com.guigarage.platform.sample.data.Topics.REMOVE_LAST;

@Service
@ApplicationScope
public class DataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataService.class);

    private final List<DataItem> items = new CopyOnWriteArrayList<>();

    private final RemotingEventBus remotingEventBus;

    private final AtomicLong idCounter = new AtomicLong();

    @Autowired
    public DataService(final RemotingEventBus remotingEventBus) {
        this.remotingEventBus = remotingEventBus;
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("Initialize");
        add();
    }

    public List<DataItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public DataItem find(final Long id) {
        return items.stream().filter(i -> i.getId().equals(id)).findAny().orElse(null);
    }

    public void clear() {
        LOGGER.debug("Removing all items");
        items.clear();
        remotingEventBus.publish(CLEAR, "moo");
    }

    public void add() {
        int itemCount = 6;
        final CopyOnWriteArrayList<DataItem> newItems = new CopyOnWriteArrayList<>();
        LOGGER.debug("Adding {} items", itemCount);
        IntStream.range(0, itemCount).forEach(i -> {
            final long id = idCounter.incrementAndGet();
            final DataItem item = new DataItem();
            item.setId(id);
            item.setName("Item " + id);
            item.setDescription("Die ist die Beschreibung für Item " + id + "...");
            items.add(item);
            newItems.add(item);
        });
        remotingEventBus.publish(NEW_ITEMS, newItems);
    }

    public void removeLast() {
        LOGGER.debug("Removing last item");
        if (items.size() >= 1) {
            final DataItem removed = items.remove(items.size() - 1);
            remotingEventBus.publish(REMOVE_LAST, removed);
        }

    }
}
