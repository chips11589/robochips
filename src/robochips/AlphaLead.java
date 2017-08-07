package robochips;

import robocode.*;
import robocode.control.events.BattleStartedEvent;
import robocode.util.Utils;

public class AlphaLead extends TeamRobot {

	private double _bfWidth;
	private double _bfHeight;
	private static double WALL_STICK = 140;
	private java.awt.geom.Rectangle2D.Double _fieldRect = new java.awt.geom.Rectangle2D.Double(18, 18, _bfWidth - 36,
			_bfHeight - 36);

	public void run() {
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		
		_bfWidth = getBattleFieldWidth();
		_bfHeight = getBattleFieldHeight();

		while (true) {
			turnRadarRightRadians(Double.POSITIVE_INFINITY);
			ahead(1000);

			double nextAngle = wallSmoothing(getX(), getY(), 0, 1, 1);
			turnRight(Utils.normalAbsoluteAngle(nextAngle));
		}
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		double targetAbsoluteBearing = getHeadingRadians() + e.getBearingRadians();

		// Absolute bearing to target subtracts current radar heading to get turn
		// required
		double radarTurn = targetAbsoluteBearing - getRadarHeadingRadians();

		setTurnGunRightRadians(Utils.normalRelativeAngle(targetAbsoluteBearing - getGunHeadingRadians()));
		setFire(2);

		setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn) * 2);
	}

	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		back(100);
	}

	/**
	 * x/y = current coordinates startAngle = absolute angle that tank starts off
	 * moving - this is the angle they will be moving at if there is no wall
	 * smoothing taking place. orientation = 1 if orbiting enemy clockwise, -1 if
	 * orbiting counter-clockwise smoothTowardEnemy = 1 if smooth towards enemy, -1
	 * if smooth away NOTE: this method is designed based on an orbital movement
	 * system; these last 2 arguments could be simplified in any other movement
	 * system.
	 */
	public double wallSmoothing(double x, double y, double startAngle, int orientation, int smoothTowardEnemy) {

		double angle = startAngle;

		// in Java, (-3 MOD 4) is not 1, so make sure we have some excess
		// positivity here
		angle += (4 * Math.PI);

		double testX = x + (Math.sin(angle) * WALL_STICK);
		double testY = y + (Math.cos(angle) * WALL_STICK);
		double wallDistanceX = Math.min(x - 18, _bfWidth - x - 18);
		double wallDistanceY = Math.min(y - 18, _bfHeight - y - 18);
		double testDistanceX = Math.min(testX - 18, _bfWidth - testX - 18);
		double testDistanceY = Math.min(testY - 18, _bfHeight - testY - 18);

		double adjacent = 0;
		int g = 0; // because I'm paranoid about potential infinite loops

		while (!_fieldRect.contains(testX, testY) && g++ < 25) {
			if (testDistanceY < 0 && testDistanceY < testDistanceX) {
				// wall smooth North or South wall
				angle = ((int) ((angle + (Math.PI / 2)) / Math.PI)) * Math.PI;
				adjacent = Math.abs(wallDistanceY);
			} else if (testDistanceX < 0 && testDistanceX <= testDistanceY) {
				// wall smooth East or West wall
				angle = (((int) (angle / Math.PI)) * Math.PI) + (Math.PI / 2);
				adjacent = Math.abs(wallDistanceX);
			}

			// use your own equivalent of (1 / POSITIVE_INFINITY) instead of 0.005
			// if you want to stay closer to the wall ;)
			angle += smoothTowardEnemy * orientation * (Math.abs(Math.acos(adjacent / WALL_STICK)) + 0.005);

			testX = x + (Math.sin(angle) * WALL_STICK);
			testY = y + (Math.cos(angle) * WALL_STICK);
			testDistanceX = Math.min(testX - 18, _bfWidth - testX - 18);
			testDistanceY = Math.min(testY - 18, _bfHeight - testY - 18);

			if (smoothTowardEnemy == -1) {
				// this method ended with tank smoothing away from enemy... you may
				// need to note that globally, or maybe you don't care.
			}
		}

		return angle; // you may want to normalize this
	}
}
