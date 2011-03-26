package com.nlworks.oldclock.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.HasMouseMoveHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.widgetideas.graphics.client.Color;
import com.google.gwt.widgetideas.graphics.client.GWTCanvas;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class OldClock implements EntryPoint {

	Canvas canvas = new Canvas(800, 400);
	Label fpslabel = new Label();

	public void onModuleLoad() {

		canvas.setLineWidth(6);
		canvas.setStrokeStyle(Color.BLACK);

		RootPanel.get().add(canvas);
		RootPanel.get().add(new Label("FPS:"));
		RootPanel.get().add(fpslabel);

		Draw draw = new Draw();
		draw.scheduleRepeating(1);

		final MyTime myTime = new MyTime(200, 00, 00, 05);

		canvas.addMouseMoveHandler(new MouseMoveHandler() {

			@Override
			public void onMouseMove(MouseMoveEvent event) {
				// System.out.println(event.getX() + " " + event.getY());
			}
		});

		(new Timer() {

			@Override
			public void run() {
				myTime.decrement();

			}
		}).scheduleRepeating(1000);
	}

	class Point {
		public double x = 0;
		public double y = 0;

		public Point(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public Point(Point targetEnd) {
			x = targetEnd.x;
			y = targetEnd.y;
		}
	}

	public class MyTime {
		int days, hours, minutes, seconds;
		NumberSegment numbers[] = new NumberSegment[9];

		public MyTime(int days, int hours, int minutes, int seconds) {
			this.days = days;
			this.hours = hours;
			this.minutes = minutes;
			this.seconds = seconds;

			double x = 0;

			numbers[0] = new NumberSegment(new Point(x, 5), days / 100);
			x += 55;
			numbers[1] = new NumberSegment(new Point(x, 5), (days % 100) / 10);
			x += 55;
			numbers[2] = new NumberSegment(new Point(x, 5), days % 10);
			x += 90;

			numbers[3] = new NumberSegment(new Point(x, 5), hours / 10);
			x += 55;
			numbers[4] = new NumberSegment(new Point(x, 5), hours % 10);
			x += 90;

			numbers[5] = new NumberSegment(new Point(x, 5), minutes / 10);
			x += 55;
			numbers[6] = new NumberSegment(new Point(x, 5), minutes % 10);
			x += 90;

			numbers[7] = new NumberSegment(new Point(x, 5), seconds / 10);
			x += 55;
			numbers[8] = new NumberSegment(new Point(x, 5), seconds % 10);

			for (int i = 0; i < 9; i++)
				addNumber(numbers[i]);
		}

		public void decrement() {

			seconds--;
			if (seconds < 0) {
				seconds = 59;
				minutes--;
				if (minutes < 0) {
					minutes = 59;
					hours--;
					if (hours < 0) {
						hours = 23;
						days--;
						if (days < 0) {
							days = 0;
						}
					}
				}
			}

			updateTime();
		}

		public void setTime(int days, int hours, int minutes, int seconds) {
			this.days = days;
			this.hours = hours;
			this.minutes = minutes;
			this.seconds = seconds;

			updateTime();
		}

		private void updateTime() {
			numbers[0].setNumber(days / 100);
			numbers[1].setNumber((days % 100) / 10);
			numbers[2].setNumber(days % 10);

			numbers[3].setNumber(hours / 10);
			numbers[4].setNumber(hours % 10);

			numbers[5].setNumber(minutes / 10);
			numbers[6].setNumber(minutes % 10);

			numbers[7].setNumber(seconds / 10);
			numbers[8].setNumber(seconds % 10);
		}

	}

	public Point lineIntersection(Point A, Point B, Point C, Point D) {

		double distAB, theCos, theSin, newX, ABpos;

		// Fail if either line is undefined.
		if (A.x == B.x && A.y == B.y || C.x == D.x && C.y == D.y)
			return null;

		// (1) Translate the system so that point A is on the origin.
		B.x -= A.x;
		B.y -= A.y;
		C.x -= A.x;
		C.y -= A.y;
		D.x -= A.x;
		D.y -= A.y;

		// Discover the length of segment A-B.
		distAB = Math.sqrt(B.x * B.x + B.y * B.y);

		// (2) Rotate the system so that point B is on the positive X axis.
		theCos = B.x / distAB;
		theSin = B.y / distAB;
		newX = C.x * theCos + C.y * theSin;
		C.y = C.y * theCos - C.x * theSin;
		C.x = newX;
		newX = D.x * theCos + D.y * theSin;
		D.y = D.y * theCos - D.x * theSin;
		D.x = newX;

		// Fail if the lines are parallel.
		if (C.y == D.y)
			return null;

		// (3) Discover the position of the intersection point along line A-B.
		ABpos = D.x + (C.x - D.x) * D.y / (D.y - C.y);

		// (4) Apply the discovered position to line A-B in the original
		// coordinate system.
		Point R = new Point(A.x + ABpos * theCos, A.y + ABpos * theSin);

		// Success.
		return R;
	}

	public class NumberSegment {
		private Segment[] segments = new Segment[7];
		private Point startPosition;
		private int prevNumber, newNumber;

		/**
		 * <pre>
		 *   ---0
		 *  3   4 
		 *  |   |
		 *  |   |
		 *   ---1
		 *  5   6
		 *  |   |
		 *  |   |
		 *   ---2
		 * </pre>
		 * 
		 * @param startPosition
		 * @param number
		 */

		public NumberSegment(Point startPosition, int number) {
			this.startPosition = startPosition;

			segments[0] = new Segment();
			segments[1] = new Segment();
			segments[2] = new Segment();
			segments[3] = new Segment();
			segments[4] = new Segment();
			segments[5] = new Segment();
			segments[6] = new Segment();
			setGoodPosition();

			setNumber(number);
		}

		/**
		 */
		public void setGoodPosition() {
			segments[0].resetPosition(startPosition.x + 4, startPosition.y + 0, true);
			segments[1].resetPosition(startPosition.x + 4, startPosition.y + 43, true);
			segments[2].resetPosition(startPosition.x + 4, startPosition.y + 86, true);
			segments[3].resetPosition(startPosition.x + 0, startPosition.y + 4, false);
			segments[4].resetPosition(startPosition.x + 43, startPosition.y + 4, false);
			segments[5].resetPosition(startPosition.x + 0, startPosition.y + 47, false);
			segments[6].resetPosition(startPosition.x + 43, startPosition.y + 47, false);
		}

		/**
		 * @param number
		 */
		public void setNumber(int number) {
			this.newNumber = number;

			for (int i = 0; i < 7; i++)
				segments[i].completeCurrentAnimation();

			switch (newNumber) {
				case 0:
					switch (prevNumber) {
						case 9:

							break;

						default:
							System.out.println("Unknown number transition");
					}

					break;
				case 1:
					segments[0].draw = false;
					segments[1].draw = false;
					segments[2].draw = false;
					segments[3].draw = false;
					segments[4].draw = true;
					segments[5].draw = false;
					segments[6].draw = true;
					break;
				case 2:
					segments[0].draw = true;
					segments[1].draw = true;
					segments[2].draw = true;
					segments[3].draw = false;
					segments[4].draw = true;
					segments[5].draw = true;
					segments[6].draw = false;
					break;
				case 3:
					segments[0].draw = true;
					segments[1].draw = true;
					segments[2].draw = true;
					segments[3].draw = false;
					segments[4].draw = true;
					segments[5].draw = false;
					segments[6].draw = true;
					break;
				case 4:
					segments[0].draw = false;
					segments[1].draw = true;
					segments[2].draw = false;
					segments[3].draw = true;
					segments[4].draw = true;
					segments[5].draw = false;
					segments[6].draw = true;
					break;
				case 5:
					segments[0].draw = true;
					segments[1].draw = true;
					segments[2].draw = true;
					segments[3].draw = true;
					segments[4].draw = false;
					segments[5].draw = false;
					segments[6].draw = true;
					break;
				case 6:
					segments[0].draw = true;
					segments[1].draw = true;
					segments[2].draw = true;
					segments[3].draw = true;
					segments[4].draw = false;
					segments[5].draw = true;
					segments[6].draw = true;
					break;
				case 7:
					segments[0].draw = true;
					segments[1].draw = false;
					segments[2].draw = false;
					segments[3].draw = false;
					segments[4].draw = true;
					segments[5].draw = false;
					segments[6].draw = true;
					break;
				case 8:
					segments[0].draw = true;
					segments[1].draw = true;
					segments[2].draw = true;
					segments[3].draw = true;
					segments[4].draw = true;
					segments[5].draw = true;
					segments[6].draw = true;
					break;
				case 9:
					segments[0].draw = true;
					segments[1].draw = true;
					segments[2].draw = true;
					segments[3].draw = true;
					segments[4].draw = true;
					segments[5].draw = false;
					segments[6].draw = true;
					break;
				default:
					System.out.println("invalid number");
			}
		}
	}

	public void addNumber(NumberSegment number) {
		Segment[] s = number.segments;
		ll.add(s[0]);
		ll.add(s[1]);
		ll.add(s[2]);
		ll.add(s[3]);
		ll.add(s[4]);
		ll.add(s[5]);
		ll.add(s[6]);
	}

	public class MoveToObject {
		public boolean rotateOnStart, rotateNegative;
		public boolean doNothing;

		public void set(boolean rotateOnStart, boolean rotateNegative) {
			this.rotateOnStart = rotateOnStart;
			this.rotateNegative = rotateNegative;
			doNothing = false;
		}
	}

	public class Segment {
		public Point baseStart, currentStart, targetEnd, currentEnd;
		public boolean draw = true;
		public boolean horizontal;

		public static final double INNERLENGTH = 4d;
		public static final double OUTERLENGTH = 35d;
		public static final double TOTALLENGHT = INNERLENGTH + OUTERLENGTH;

		MoveToObject moveQueue[] = new MoveToObject[2];
		private int queuePos = 0;
		private int queueLen = 0;

		public Segment() {
			moveQueue[0] = new MoveToObject();
			moveQueue[1] = new MoveToObject();
		}

		public void resetPosition(double x, double y, boolean horizontal) {
			this.horizontal = horizontal;

			if (horizontal)
				baseStart = new Point(x - INNERLENGTH, y);
			else
				baseStart = new Point(x, y - INNERLENGTH);

			currentStart = new Point(x, y);
			setInitialPoint(x, y);
			currentEnd = new Point(targetEnd);
		}

		public void addFillerMove() {
			moveQueue[queueLen].doNothing = true;
			queueLen++;
		}

		public void addMoveTo(boolean rotateOnStart, boolean rotateNegative) {

			moveQueue[queueLen].set(rotateOnStart, rotateNegative);
			queueLen++;
		}

		public void animateCurrentMoveTo(double sin, double cos) {
			if (queuePos >= queueLen || moveQueue[queuePos].doNothing)
				return;

			setAngle(sin, cos, moveQueue[queuePos].rotateOnStart, moveQueue[queuePos].rotateNegative);
		}

		public void completeCurrentAnimation() {
			if (queuePos >= queueLen)
				return;

			if (!moveQueue[queuePos].doNothing)
				// sin(90)=1, cos(90)=0
				setAngle(1, 0, moveQueue[queuePos].rotateOnStart, moveQueue[queuePos].rotateNegative);

			queuePos++;
		}

		public void completeAllAnimations() {

			for (; queuePos < queueLen; queuePos++)
				if (!moveQueue[queuePos].doNothing)
					setAngle(0, 1, moveQueue[queuePos].rotateOnStart, moveQueue[queuePos].rotateNegative);

			queuePos = 0;
			queueLen = 0;
		}

		public void setInitialPoint(double x, double y) {
			if (horizontal) {
				targetEnd = new Point(x + OUTERLENGTH, y);
			} else {
				targetEnd = new Point(x, y + OUTERLENGTH);
			}
		}

		private void setAngle(double sin, double cos, boolean rotateOnStart, boolean rotateNegative) {
			if (rotateNegative) {
				cos *= -1;
				sin *= -1;
			}

			if (rotateOnStart) {
				currentStart.x = baseStart.x + ((INNERLENGTH) * cos);
				currentStart.y = baseStart.y + ((INNERLENGTH) * sin);

				currentEnd.x = baseStart.x + ((TOTALLENGHT) * cos);
				currentEnd.y = baseStart.y + ((TOTALLENGHT) * sin);
			} else {
				currentStart.x = baseStart.x + ((INNERLENGTH) * sin);
				currentStart.y = baseStart.y + ((INNERLENGTH) * cos);

				currentEnd.x = baseStart.x + ((TOTALLENGHT) * sin);
				currentEnd.y = baseStart.y + ((TOTALLENGHT) * cos);
			}

			// System.out.println(baseStart.x + "x" + baseStart.y + "y   " + currentEnd.x + "x" + currentEnd.y + "y   "
			// + angle + "angle");
		}
	}

	LinkedList<Segment> ll = new LinkedList<Segment>();

	public class Draw extends Timer {

		int x = 1;
		int callcount = 0;
		long time = System.currentTimeMillis();
		double sin, cos;

		@Override
		public void run() {

			callcount++;

			long curtime = System.currentTimeMillis();
			int timediff = (int) (curtime - time);
			if (timediff >= 1000) {
				fpslabel.setText(Integer.toString(callcount));
				time = curtime;
				callcount = 0;
				timediff = 0;
				// System.out.println("============ RESET ============");
			}

			if (timediff < 500) {
				double angle = Math.toRadians(2 * timediff * -0.09d);
				ll.getLast().setAngle(Math.sin(angle), Math.cos(angle));
			} else {
				double angle = Math.toRadians((2000 - 2 * timediff) * -0.09d);
				// ll.getLast().setAngle(Math.sin(angle), Math.cos(angle));
				ll.getLast().setAngle(0, 1);
			}

			canvas.saveContext();

			canvas.clear();
			canvas.setStrokeStyle(Color.BLACK);

			for (Segment segment : ll) {
				if (!segment.draw)
					continue;

				if (segment == ll.getLast())
					canvas.setStrokeStyle(Color.GREEN);

				canvas.beginPath();
				{
					canvas.moveTo(segment.currentStart.x, segment.currentStart.y);
					canvas.lineTo(segment.currentEnd.x, segment.currentEnd.y);
					canvas.closePath();
				}
				canvas.stroke();

			}

			canvas.restoreContext();

		}
	}

	public class Canvas extends GWTCanvas implements HasMouseMoveHandlers {

		public Canvas() {
			super();
		}

		public Canvas(int coordX, int coordY) {
			super(coordX, coordY);
		}

		public Canvas(int coordX, int coordY, int pixelX, int pixelY) {
			super(coordX, coordY, pixelX, pixelY);
		}

		@Override
		public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
			return addDomHandler(handler, MouseMoveEvent.getType());
		}
	}
}
