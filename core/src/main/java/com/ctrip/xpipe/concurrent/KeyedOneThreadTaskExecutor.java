package com.ctrip.xpipe.concurrent;

import java.util.Map;
import java.util.Map.Entry;

import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.xpipe.api.command.Command;
import com.ctrip.xpipe.api.factory.ObjectFactory;
import com.ctrip.xpipe.api.lifecycle.Destroyable;
import com.ctrip.xpipe.utils.MapUtils;

/**
 * @author wenchao.meng
 *
 * Jan 3, 2017
 */
public class KeyedOneThreadTaskExecutor<K> implements Destroyable{
	
	private static Logger logger = LoggerFactory.getLogger(KeyedOneThreadTaskExecutor.class);
	
	private Map<K, OneThreadTaskExecutor> keyedExecutor = new ConcurrentHashMap<>();
	
	private String taskDesc;
	
	public KeyedOneThreadTaskExecutor(String taskDesc){
		this.taskDesc = taskDesc;
	}
	
	public void execute(K key, Command<?> command){
		
		OneThreadTaskExecutor oneThreadTaskExecutor = getOrCreate(key);
		oneThreadTaskExecutor.executeCommand(command);
	}

	
	private OneThreadTaskExecutor getOrCreate(K key) {
		
		return MapUtils.getOrCreate(keyedExecutor, key, new ObjectFactory<OneThreadTaskExecutor>() {
			
			@Override
			public OneThreadTaskExecutor create() {
				return new OneThreadTaskExecutor(taskDesc);
			}
		});
	}

	@Override
	public void destroy() throws Exception {
		
		logger.info("[destroy]{}", this);
		for(Entry<K, OneThreadTaskExecutor> entry : keyedExecutor.entrySet()){
			entry.getValue().destroy();
		}
	}
}
