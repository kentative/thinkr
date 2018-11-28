package com.bytes.fmk.service.leaderboard.ledger.impl;

import java.util.Objects;

class RecordableInfo {
	String name;
	int points;
	
	public RecordableInfo(String categoryName, int points) {
		this.name = categoryName;
		this.points = points;
	}

	@Override
	public boolean equals(Object o) {
		
		if(!(o instanceof RecordableInfo)) {
			return false;
		}
		
		RecordableInfo recordable = (RecordableInfo) o;
		return 
				name.equalsIgnoreCase(recordable.name) &&
				points == recordable.points;
	}
	
	public int hashCode() {
		return Objects.hash(name, points);
	}
}