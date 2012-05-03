package edu.illinois.mitra.starl.harness;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import edu.illinois.mitra.starl.interfaces.SimComChannel;


public class DecoupledSimComChannel implements SimComChannel  {
	private static final String BROADCAST_IP = "192.168.1.255";
	
	private Map<String, SimSmartComThread> receivers;

	private Set<DeliveryEvent> msgs = new HashSet<DeliveryEvent>();
	private Set<DeliveryEvent> toAdd = new HashSet<DeliveryEvent>();
	
	
	// Message loss and delay stats
	private int dropRate;
	private int meanDelay;
	private int delayStdDev;
	
	// Message statistics
	private int stat_totalMessages = 0;
	private int stat_bcastMessages = 0;
	private int stat_lostMessages = 0;
	private int stat_overallDelay = 0;
	
	private Random rand;
	
	// Drop rate is per 100 messages
	public DecoupledSimComChannel(int meanDelay, int delayStdDev, int dropRate, int seed) {
		this.meanDelay = meanDelay;
		this.dropRate = (100-dropRate);
		this.delayStdDev = delayStdDev;
		receivers = new HashMap<String,SimSmartComThread>();
		rand = new Random(seed);
	}

	public void registerMsgReceiver(SimSmartComThread hct, String IP) {
		receivers.put(IP, hct);
	}

	public void removeMsgReceiver(String IP) {
		receivers.remove(IP);
	}
	
	private synchronized void addInTransit(String msg, String IP) {
		stat_totalMessages ++;
		if(meanDelay > 0) {
			if(rand.nextInt(100) < dropRate) {
				int delay = Math.max(1,(rand.nextInt(2*delayStdDev+1)-delayStdDev)+meanDelay);
				toAdd.add(new DeliveryEvent(msg,delay,receivers.get(IP)));
				stat_overallDelay += delay;
			} else {
				stat_lostMessages ++;
			}
		} else if(receivers.containsKey(IP)){
			receivers.get(IP).receive(msg);
		}
	}
	
	public void sendMsg(String from, String msg, String IP) {		
		if(IP.equals(BROADCAST_IP)) {
			stat_bcastMessages ++;
			for(String ip : receivers.keySet()) {
				if(!ip.equals(from)) {
					addInTransit(msg, ip);
				}
			}
		} else if(receivers.containsKey(IP)) {
			addInTransit(msg,IP);
		}
	}

	// Must not advance by more than the minimum delay!!
	public void advanceTime(long advance) {
		Set<DeliveryEvent> toRemove = new HashSet<DeliveryEvent>();
		for(DeliveryEvent de : msgs) {
			de.delay -= advance;
			// If this message's delay has expired, deliver it
			// and flag it for removal
			if(de.delay == 0) {
				de.deliver();
				toRemove.add(de);
			} else if(de.delay < 0) {
				throw new RuntimeException("ENCOUNTERED A NEGATIVE MESSAGE DELAY! TIME ADVANCED BY MORE THAN MINIMUM DELAY!");
			}
		}
		// Remove all delivered messages
		msgs.removeAll(toRemove);
	}
	
	public long minDelay() {
		Long min = Long.MAX_VALUE;
		int retries = 0;
		while(!toAdd.isEmpty() && retries < 5) {
			try {
				synchronized(msgs) {
					msgs.addAll(toAdd);
					toAdd.clear();					
				}
			} catch(ConcurrentModificationException e) {
				e.printStackTrace(System.out);
				System.out.println("\n\tOh snap! Don't worry, everything is probably ok.");
				retries ++;
			}
		}
		for(DeliveryEvent de : msgs) {
			min = Math.min(min, de.delay);
		}
		
		return min;
	}
	
	private class DeliveryEvent {
		private SimSmartComThread recipientThread;
		private String msg;
		public long delay = 0;

		public DeliveryEvent(String msg, long delay, SimSmartComThread recipientThread) {
			this.delay = delay;
			this.msg = msg;
			this.recipientThread = recipientThread;
		}
		public void deliver() {
			recipientThread.receive(msg);
		}
		private DecoupledSimComChannel getOuterType() {
			return DecoupledSimComChannel.this;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((msg == null) ? 0 : msg.hashCode());
			result = prime
					* result
					+ ((recipientThread == null) ? 0 : recipientThread
							.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DeliveryEvent other = (DeliveryEvent) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (msg == null) {
				if (other.msg != null)
					return false;
			} else if (!msg.equals(other.msg))
				return false;
			if (recipientThread == null) {
				if (other.recipientThread != null)
					return false;
			} else if (!recipientThread.equals(other.recipientThread))
				return false;
			return true;
		}
	}
	
	public void printStatistics() {
		if(stat_totalMessages > 0) {
			System.out.println("Total messages: " + stat_totalMessages);
			System.out.println("Broadcast messages: " + stat_bcastMessages);
			System.out.println("Dropped messages: " + stat_lostMessages + " = " + 100*(float)stat_lostMessages/stat_totalMessages + "%");
			System.out.println("Average delay: " + (float)stat_overallDelay/stat_totalMessages + " ms");
		} else {
			System.out.println("No messages were sent.");
		}
	}
}
