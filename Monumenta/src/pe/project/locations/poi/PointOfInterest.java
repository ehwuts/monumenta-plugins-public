package pe.project.locations.poi;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import pe.project.Constants;
import pe.project.Main;
import pe.project.locations.poi.POIConstants.POI;
import pe.project.point.Point;

public class PointOfInterest {
	protected POIConstants.POI mPOI;
	protected String mAreaName = null;
	protected Point mCenter;
	protected double mWithinRadius;
	protected double mNearbyRadius;
	protected int mTimer;

	public PointOfInterest(POIConstants.POI poi, String areaName, Point center, double withinRadius, double nearbyRadius, String description) {
		mAreaName = areaName;
		mPOI = poi;
		mCenter = center;
		mWithinRadius = withinRadius;
		mNearbyRadius = nearbyRadius;
	}

	public boolean nearPOI(Point playerLoc) {
		return _distance(playerLoc) <= mNearbyRadius;
	}

	public boolean withinPOI(Point playerLoc) {
		return _distance(playerLoc) <= mWithinRadius;
	}

	public void update(Main plugin, int ticks) {
		int oldValue = mTimer;
		if (oldValue > 0) {
			int newValue = mTimer -= ticks;
			if (oldValue > Constants.TWO_MINUTES && newValue <= Constants.TWO_MINUTES) {
				_messageNearbyPlayers(plugin, " is respawning in 2 Minutes!");
			} else if (oldValue > Constants.THIRTY_SECONDS && newValue <= Constants.THIRTY_SECONDS) {
				_messageNearbyPlayers(plugin, " is respawning in 30 Seconds!");
			} else if (oldValue > 0 && newValue <= 0) {
				mTimer = 0;
				save();
			}
		}
	}

	public String getName() {
		return mAreaName;
	}

	public POI getPOI() {
		return mPOI;
	}

	public int getTimer() {
		return mTimer;
	}

	public void setTimer(int value) {
		mTimer = value;
	}

	public void save() {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Set<Score> scores = scoreboard.getScores(mPOI.mScoreboard);

		for (Score score : scores) {
			if (score.getObjective().getDisplayName().contains("POITimers")) {
				score.setScore(mTimer);
				break;
			}
		}
	}

	public void load() {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Set<Score> scores = scoreboard.getScores(mPOI.mScoreboard);

		for(Score score : scores) {
			if (score.getObjective().getDisplayName().contains("POITimers")) {
				mTimer = score.getScore();
				break;
			}
		}
	}

	public double _distance(Point point) {
		return Math.sqrt(
			((point.mX - mCenter.mX) * (point.mX - mCenter.mX)) +
			((point.mY - mCenter.mY) * (point.mY - mCenter.mY)) +
			((point.mZ - mCenter.mZ) * (point.mZ - mCenter.mZ))
		);
	}

	private void _messageNearbyPlayers(Main plugin, String suffix) {
		String message = ChatColor.RED + "" + ChatColor.BOLD + mAreaName + suffix;
		for (Player player : plugin.mTrackingManager.mPlayers.getPlayers()) {
			if (nearPOI(new Point(player.getLocation()))) {
				player.sendMessage(message);
			}
		}
	}
}
