package com.nlworks.oldclock.client;

import java.util.LinkedList;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.HasMouseMoveHandlers;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.widgetideas.graphics.client.Color;
import com.google.gwt.widgetideas.graphics.client.GWTCanvas;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class OldClock implements EntryPoint {

	Canvas canvas = new Canvas(800, 400);
	Label fpslabel = new Label();

	int n = 8;
	public static final int delay = 10000;

	public void onModuleLoad() {

		canvas.setLineWidth(6);
		canvas.setStrokeStyle(Color.BLACK);

		RootPanel.get().add(canvas);
		RootPanel.get().add(new Label("FPS:"));
		RootPanel.get().add(fpslabel);

		Draw draw = new Draw();
		draw.scheduleRepeating(1);

		// final MyTime myTime = new MyTime(200, 00, 00, 05);

		final NumberSegment segment = new NumberSegment(new Point(20, 20), n);
		addNumber(segment);

		canvas.addMouseMoveHandler(new MouseMoveHandler() {

			@Override
			public void onMouseMove(MouseMoveEvent event) {
				// System.out.println(event.getX() + " " + event.getY());
			}
		});

		(new Timer() {

			@Override
			public void run() {
				// myTime.decrement();
				n--;
				if (n < 0)
					n = 9;
				segment.setNumber(n);
			}
		}).scheduleRepeating(delay);

		(new Timer() {

			@Override
			public void run() {
				n--;
				if (n < 0)
					n = 9;
				segment.setNumber(n);
			}
		}).schedule(1000);

	}

	class Point {
		public double x = 0;
		public double y = 0;

		public Point(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public Point(Point point) {
			x = point.x;
			y = point.y;
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

			prevNumber = number;
			newNumber = number;
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

			for (int i = 0; i < 7; i++)
				segments[i].completeAllAnimations();

			prevNumber = newNumber;
			this.newNumber = number;

			switch (newNumber) {
				case 0:
					switch (prevNumber) {
						case 1:
							segments[0].addMoveTo(false, false);
							segments[1].addMoveTo(true, true);
							segments[1].addMoveTo(false, true);
							segments[2].addMoveTo(false, true);
							segments[3].addMoveTo(false, false);
							segments[3].addMoveTo(true, false);
							segments[5].addMoveTo(true, true);
							segments[5].addMoveTo(false, true);
							break;

						default:
							System.out.println("Unknown number transition.  newNumber=" + newNumber + "  prevNumber=" + prevNumber);
					}
					break;

				case 1:
					switch (prevNumber) {
						case 2:
							segments[3].addMoveTo(false, true);
							segments[1].addMoveTo(false, true);
							segments[2].addFillerMove();
							segments[2].addMoveTo(false, false);
							segments[5].addMoveTo(false, true);
							segments[5].addMoveTo(true, false);
							break;

						default:
							System.out.println("Unknown number transition.  newNumber=" + newNumber + "  prevNumber=" + prevNumber);
					}
					break;

				case 2:
					switch (prevNumber) {
						case 3:
							segments[5].addMoveTo(false, true);
							segments[6].addMoveTo(false, true);
							break;

						default:
							System.out.println("Unknown number transition.  newNumber=" + newNumber + "  prevNumber=" + prevNumber);
					}
					break;

				case 3:
					switch (prevNumber) {
						case 4:
							segments[2].addMoveTo(false, true);
							segments[3].addMoveTo(true, true);
							segments[5].addMoveTo(true, true);
							break;

						default:
							System.out.println("Unknown number transition.  newNumber=" + newNumber + "  prevNumber=" + prevNumber);
					}
					break;

				case 4:
					switch (prevNumber) {
						case 5:
							segments[0].addMoveTo(false, true);
							segments[4].addMoveTo(true, true);
							segments[5].addMoveTo(true, false);
							break;

						default:
							System.out.println("Unknown number transition.  newNumber=" + newNumber + "  prevNumber=" + prevNumber);
					}
					break;

				case 5:
					switch (prevNumber) {
						case 6:
							segments[5].addMoveTo(false, false);
							break;

						default:
							System.out.println("Unknown number transition.  newNumber=" + newNumber + "  prevNumber=" + prevNumber);
					}
					break;

				case 6:
					switch (prevNumber) {
						case 7:
							segments[1].addMoveTo(false, false);
							segments[2].addMoveTo(false, true);
							segments[3].addMoveTo(true, false);
							segments[4].addMoveTo(true, false);
							segments[5].addMoveTo(true, true);
							segments[5].addMoveTo(false, true);
							break;

						default:
							System.out.println("Unknown number transition.  newNumber=" + newNumber + "  prevNumber=" + prevNumber);
					}
					break;

				case 7:
					switch (prevNumber) {
						case 8:
							segments[1].addMoveTo(false, false);
							segments[2].addFillerMove();
							segments[2].addMoveTo(false, false);
							segments[3].addMoveTo(true, true);
							segments[5].addMoveTo(false, false);
							segments[5].addMoveTo(true, false);
							break;

						default:
							System.out.println("Unknown number transition.  newNumber=" + newNumber + "  prevNumber=" + prevNumber);
					}
					break;

				case 8:
					switch (prevNumber) {
						case 9:
							segments[5].addMoveTo(true, false);
							break;

						default:
							System.out.println("Unknown number transition.  newNumber=" + newNumber + "  prevNumber=" + prevNumber);
					}
					break;

				case 9:
					switch (prevNumber) {
						case 0:
							segments[5].addMoveTo(true, true);
							segments[1].addMoveTo(true, true);
							break;

						default:
							System.out.println("Unknown number transition.  newNumber=" + newNumber + "  prevNumber=" + prevNumber);
					}
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
		public boolean rotateOnStart, rotatePositive;
		public boolean doNothing;

		public void set(boolean rotateOnStart, boolean rotateNegative) {
			this.rotateOnStart = rotateOnStart;
			this.rotatePositive = rotateNegative;
			doNothing = false;
		}
	}

	public class Segment {
		public Point baseStart, baseEnd, currentStart, currentEnd;
		public boolean draw = true;
		public boolean horizontal;

		public static final double INNERLENGTH = 4d;
		public static final double OUTERLENGTH = 35d;
		public static final double TOTALLENGHT = INNERLENGTH + OUTERLENGTH + INNERLENGTH;

		MoveToObject moveQueue[] = new MoveToObject[2];
		private int queuePos = 0;
		private int queueLen = 0;

		private void setAngle(double sin, double cos, boolean rotateOnStart, boolean rotatePositive) {
			if (!rotatePositive) {
				cos *= -1;
				sin *= -1;
			}

			if (rotateOnStart && horizontal) {
				currentStart.x = baseStart.x + ((INNERLENGTH) * cos);
				currentStart.y = baseStart.y + ((INNERLENGTH) * sin);

				currentEnd.x = baseStart.x + ((OUTERLENGTH + INNERLENGTH) * cos);
				currentEnd.y = baseStart.y + ((OUTERLENGTH + INNERLENGTH) * sin);
			} else if (rotateOnStart && !horizontal) {
				currentEnd.x = baseEnd.x + ((INNERLENGTH) * cos);
				currentEnd.y = baseEnd.y + ((INNERLENGTH) * sin);

				currentStart.x = baseEnd.x + ((OUTERLENGTH + INNERLENGTH) * cos);
				currentStart.y = baseEnd.y + ((OUTERLENGTH + INNERLENGTH) * sin);
			} else if (!rotateOnStart && horizontal) {
				currentStart.x = baseStart.x + ((INNERLENGTH) * sin);
				currentStart.y = baseStart.y + ((INNERLENGTH) * cos);

				currentEnd.x = baseStart.x + ((OUTERLENGTH + INNERLENGTH) * sin);
				currentEnd.y = baseStart.y + ((OUTERLENGTH + INNERLENGTH) * cos);
			} else {
				currentEnd.x = baseEnd.x + ((INNERLENGTH) * sin);
				currentEnd.y = baseEnd.y + ((INNERLENGTH) * cos);

				currentStart.x = baseEnd.x + ((OUTERLENGTH + INNERLENGTH) * sin);
				currentStart.y = baseEnd.y + ((OUTERLENGTH + INNERLENGTH) * cos);
			}

			// System.out.println(baseStart.x + "x" + baseStart.y + "y   " + currentEnd.x + "x" + currentEnd.y + "y   "
			// + angle + "angle");
		}

		public Segment() {
			moveQueue[0] = new MoveToObject();
			moveQueue[1] = new MoveToObject();
		}

		public void resetPosition(double x, double y, boolean horizontal) {
			this.horizontal = horizontal;

			if (horizontal) {
				baseStart = new Point(x - INNERLENGTH, y);
				baseEnd = new Point(x + INNERLENGTH + OUTERLENGTH, y);
				currentStart = new Point(x, y);
				currentEnd = new Point(x + OUTERLENGTH, y);
			} else {
				baseStart = new Point(x, y - INNERLENGTH);
				baseEnd = new Point(x, y + INNERLENGTH + OUTERLENGTH);
				currentStart = new Point(x, y);
				currentEnd = new Point(x, y + OUTERLENGTH);
			}

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

			setAngle(sin, cos, moveQueue[queuePos].rotateOnStart, moveQueue[queuePos].rotatePositive);
		}

		public void completeCurrentAnimation() {
			if (queuePos >= queueLen)
				return;

			if (!moveQueue[queuePos].doNothing) {
				// sin(90)=1, cos(90)=0
				setAngle(1, 0, moveQueue[queuePos].rotateOnStart, moveQueue[queuePos].rotatePositive);
				horizontal = !horizontal;
			}

			queuePos++;
		}

		public void completeAllAnimations() {

			for (; queuePos < queueLen; queuePos++)
				if (!moveQueue[queuePos].doNothing) {
					setAngle(0, 1, moveQueue[queuePos].rotateOnStart, moveQueue[queuePos].rotatePositive);
					horizontal = !horizontal;
				}

			queuePos = 0;
			queueLen = 0;
		}
	}

	LinkedList<Segment> ll = new LinkedList<Segment>();

	public class Draw extends Timer {

		int x = 1;
		int callcount = 0;
		long time = System.currentTimeMillis();
		double sin, cos;
		boolean stage1 = true, stage2 = false;

		@Override
		public void run() {

			callcount++;

			long curtime = System.currentTimeMillis();
			int timediff = (int) (curtime - time);
			if (timediff >= delay) {
				fpslabel.setText(Integer.toString(callcount));
				time = curtime;
				callcount = 0;
				timediff = 0;
				stage1 = true;
				stage2 = false;
				// System.out.println("============ RESET ============");
			}

			if (timediff < (delay / 2)) {
				stage1 = true;
				double angle = Math.toRadians(timediff * (90.0d / (delay / 2.0d)));
				sin = Math.sin(angle);
				cos = Math.cos(angle);

			} else {
				stage2 = true;
				double angle = Math.toRadians((timediff - delay / 2) * (90.0d / (delay / 2.0d)));
				sin = Math.sin(angle);
				cos = Math.cos(angle);
			}

			if (stage1 && stage2) {
				for (Segment segment : ll)
					segment.completeCurrentAnimation();
				stage1 = false;
			}

			canvas.saveContext();

			canvas.clear();

			Color c = Color.BLACK;

			for (Segment segment : ll) {
				if (!segment.draw)
					continue;

				if (c == Color.BLACK)
					c = Color.BLUE;
				else if (c == Color.BLUE)
					c = Color.CYAN;
				else if (c == Color.CYAN)
					c = Color.DARK_ORANGE;
				else if (c == Color.DARK_ORANGE)
					c = Color.GREEN;
				else if (c == Color.GREEN)
					c = Color.GREY;
				else if (c == Color.GREY)
					c = Color.PINK;
				else if (c == Color.PINK)
					c = Color.RED;
				else if (c == Color.RED)
					c = Color.YELLOW;
				else if (c == Color.YELLOW)
					c = Color.BLACK;

				canvas.setStrokeStyle(c);

				segment.animateCurrentMoveTo(sin, cos);

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
