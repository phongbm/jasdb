package nl.renarj.core.statistics;

import java.util.concurrent.TimeUnit;

public interface StatTimeProvider {
	public long getCurrentTime();
	
	public long formatTime(long timeDifference, TimeUnit unit);
}
