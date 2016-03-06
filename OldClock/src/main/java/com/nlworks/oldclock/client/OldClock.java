package com.nlworks.oldclock.client;

import java.util.LinkedList;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class OldClock implements EntryPoint {

	Canvas canvas = Canvas.createIfSupported();
	Context2d context = canvas.getContext2d();
	//(1000, 500);
	Label fpslabel = new Label();
	boolean decrament = false;

	public static final int delay = 1000;

	MyTime myTime;

	ImageElement dotIcon;

	public void onModuleLoad() {

		context.setLineWidth(6);
		context.setStrokeStyle("BLACK");

		RootPanel.get().add(canvas);
		RootPanel.get().add(new Label("FPS:"));
		RootPanel.get().add(fpslabel);
		

	    final TextBox days = new TextBox();
	    days.setText("200");
	    final TextBox hours = new TextBox();
	    hours.setText("0");
	    final TextBox minutes = new TextBox();
	    minutes.setText("0");
	    final TextBox seconds = new TextBox();
	    seconds.setText("20");
	    final DialogBox box = new DialogBox();
	    
	    Grid g = new Grid(5,2);
	    box.setWidget(g);
	    g.setText(0, 0, "Days");
	    g.setWidget(0, 1, days);
	    
	    g.setText(1, 0, "Hours");
	    g.setWidget(1, 1, hours);
	    
	    g.setText(2, 0, "Minutes");
	    g.setWidget(2, 1, minutes);
	    
	    g.setText(3, 0, "Seconds");
	    g.setWidget(3, 1, seconds);
	    
	    Button b = new Button("Set Time");
	    g.setWidget(4, 1, b);

	    b.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
			    Image img = new Image("circle.png");
			    dotIcon = ImageElement.as(img.getElement());
			    img.addLoadHandler(new LoadHandler() {
					@Override
					public void onLoad(LoadEvent event) {
						int d = Integer.valueOf(days.getText());
						int h = Integer.valueOf(hours.getText());
						int m = Integer.valueOf(minutes.getText());
						int s = Integer.valueOf(seconds.getText());
						
						if (d < 0 || d > 365 || h < 0 || h > 23 || m < 0 || m > 59 || s < 0 || s > 59)
							return;
						
						box.setVisible(false);

						Draw draw = new Draw();
						draw.scheduleRepeating(10);

						myTime = new MyTime(d, h, m, s);

						(new Timer() {

							@Override
							public void run() {
								// myTime.decrement();
								decrament = true;
							}
						}).scheduleRepeating(delay);
					}
			    });
			}
		});
	    
	    box.show();
	    


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

		public void copy(Point point) {
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

			double x = 20;
			double y = 20;

			numbers[0] = new NumberSegment(new Point(x, y), days / 100);
			x += 55;
			numbers[1] = new NumberSegment(new Point(x, y), (days % 100) / 10);
			x += 55;
			numbers[2] = new NumberSegment(new Point(x, y), days % 10);
			x += 95;

			numbers[3] = new NumberSegment(new Point(x, y), hours / 10);
			x += 55;
			numbers[4] = new NumberSegment(new Point(x, y), hours % 10);
			x += 95;

			numbers[5] = new NumberSegment(new Point(x, y), minutes / 10);
			x += 55;
			numbers[6] = new NumberSegment(new Point(x, y), minutes % 10);
			x += 95;

			numbers[7] = new NumberSegment(new Point(x, y), seconds / 10);
			x += 55;
			numbers[8] = new NumberSegment(new Point(x, y), seconds % 10);

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

			segments[0] = new Segment(0);
			segments[1] = new Segment(1);
			segments[2] = new Segment(2);
			segments[3] = new Segment(3);
			segments[4] = new Segment(4);
			segments[5] = new Segment(5);
			segments[6] = new Segment(6);
			setGoodPosition();

			prevNumber = 8;
			newNumber = 8;

			for (int i = 8; newNumber != number; i--) {
				setNumber(i);
				for (int j = 0; j < 7; j++)
					segments[j].completeAllAnimations();
			}

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

			if (prevNumber == newNumber)
				return;

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
							segments[5].addMoveTo(false, false);
							segments[5].addMoveTo(true, false);
							segments[6].addMoveTo(false, false);
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

						case 0:
							segments[0].addMoveTo(false, true);
							segments[1].addMoveTo(true, true);
							segments[3].addMoveTo(true, true);
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

						case 0:
							segments[0].addMoveTo(false, true);
							segments[1].addMoveTo(true, true);
							segments[3].addMoveTo(true, true);
							segments[5].addMoveTo(false, false);
							break;

						default:
							System.out.println("Unknown number transition.  newNumber=" + newNumber + "  prevNumber=" + prevNumber);
					}
					break;

				case 4:
					switch (prevNumber) {
						case 5:
							segments[0].addMoveTo(false, true);
							segments[2].addMoveTo(false, false);
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

						case 0:
							segments[1].addMoveTo(true, true);
							segments[4].addMoveTo(true, false);
							segments[5].addMoveTo(false, false);
							break;

						default:
							System.out.println("Unknown number transition.  newNumber=" + newNumber + "  prevNumber=" + prevNumber);
					}
					break;

				case 6:
					switch (prevNumber) {
						case 7:
							segments[1].addMoveTo(false, true);
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

		public void set(boolean rotateOnStart, boolean rotatePositive) {
			this.rotateOnStart = rotateOnStart;
			this.rotatePositive = rotatePositive;
			doNothing = false;
		}
	}

	public class Segment {
		public Point baseStart, baseEnd, currentStart, currentEnd;
		public boolean draw = true;
		public boolean horizontal;

		public static final double PADDING_LENGTH = 4d;
		public static final double OUTERLENGTH = 35d;
		public static final double TOTALLENGHT = PADDING_LENGTH + OUTERLENGTH + PADDING_LENGTH;

		MoveToObject moveQueue[] = new MoveToObject[2];
		private int queuePos = 0;
		private int queueLen = 0;

		private int seg;

		private void setAngle(double angle, boolean rotateOnStart, boolean rotatePositive) {

			double cos, sin, add = 0;

			if (horizontal) {
				if (rotateOnStart) {
					if (baseStart.x <= baseEnd.x)
						add = 0;
					else
						add = 180;
				} else {
					if (baseStart.x <= baseEnd.x)
						add = 180;
					else
						add = 0;
				}

			} else {
				if (rotateOnStart) {
					if (baseStart.y <= baseEnd.y)
						add = 90;
					else
						add = 270;
				} else {
					if (baseStart.y <= baseEnd.y)
						add = 270;
					else
						add = 90;
				}
			}

			if (rotatePositive) {
				cos = Math.cos(Math.toRadians(add - angle));
				sin = Math.sin(Math.toRadians(add - angle));
			} else {
				cos = Math.cos(Math.toRadians(add + angle));
				sin = Math.sin(Math.toRadians(add + angle));
			}

			// segments[1].addMoveTo(false, false);
			// segments[3].addMoveTo(true, true);
			// segments[5].addMoveTo(false, false);

			if (rotateOnStart) {
				currentStart.x = baseStart.x + ((PADDING_LENGTH) * cos);
				currentStart.y = baseStart.y + ((PADDING_LENGTH) * sin);

				currentEnd.x = baseStart.x + ((OUTERLENGTH + PADDING_LENGTH) * cos);
				currentEnd.y = baseStart.y + ((OUTERLENGTH + PADDING_LENGTH) * sin);
			} else {
				currentEnd.x = baseEnd.x + ((PADDING_LENGTH) * cos);
				currentEnd.y = baseEnd.y + ((PADDING_LENGTH) * sin);

				currentStart.x = baseEnd.x + ((OUTERLENGTH + PADDING_LENGTH) * cos);
				currentStart.y = baseEnd.y + ((OUTERLENGTH + PADDING_LENGTH) * sin);
			}
			// else if (rotateOnStart && !horizontal) {
			// currentStart.x = baseStart.x + ((PADDING_LENGTH) * sin);
			// currentStart.y = baseStart.y + ((PADDING_LENGTH) * cos);
			//
			// currentEnd.x = baseStart.x + ((OUTERLENGTH + PADDING_LENGTH) * sin);
			// currentEnd.y = baseStart.y + ((OUTERLENGTH + PADDING_LENGTH) * cos);
			// } else if (!rotateOnStart && horizontal) {
			// currentEnd.x = baseEnd.x + ((PADDING_LENGTH) * cos);
			// currentEnd.y = baseEnd.y + ((PADDING_LENGTH) * sin);
			//
			// currentStart.x = baseEnd.x + ((OUTERLENGTH + PADDING_LENGTH) * cos);
			// currentStart.y = baseEnd.y + ((OUTERLENGTH + PADDING_LENGTH) * sin);
			// } else if (!rotateOnStart && !horizontal) {
			// currentEnd.x = baseEnd.x + ((PADDING_LENGTH) * (sin));
			// currentEnd.y = baseEnd.y + ((PADDING_LENGTH) * (cos));
			//
			// currentStart.x = baseEnd.x + ((OUTERLENGTH + PADDING_LENGTH) * (sin));
			// currentStart.y = baseEnd.y + ((OUTERLENGTH + PADDING_LENGTH) * (cos));
			// }

			// System.out.println(seg + "  " + (int) baseStart.x + "x" + (int) baseStart.y + "y   " + (int) currentEnd.x
			// + "x" + (int) currentEnd.y + "y   " + (int) angle + "angle");
		}

		public Segment(int seg) {
			this.seg = seg;
			moveQueue[0] = new MoveToObject();
			moveQueue[1] = new MoveToObject();
		}

		public void resetPosition(double x, double y, boolean horizontal) {
			this.horizontal = horizontal;

			if (horizontal) {
				baseStart = new Point(x - PADDING_LENGTH, y);
				baseEnd = new Point(x + PADDING_LENGTH + OUTERLENGTH, y);
				currentStart = new Point(x, y);
				currentEnd = new Point(x + OUTERLENGTH, y);
			} else {
				baseStart = new Point(x, y - PADDING_LENGTH);
				baseEnd = new Point(x, y + PADDING_LENGTH + OUTERLENGTH);
				currentStart = new Point(x, y);
				currentEnd = new Point(x, y + OUTERLENGTH);
			}

		}

		public void addFillerMove() {
			moveQueue[queueLen].doNothing = true;
			queueLen++;
		}

		public void addMoveTo(boolean rotateOnStart, boolean rotatePositive) {

			moveQueue[queueLen].set(rotateOnStart, rotatePositive);
			queueLen++;
		}

		public void animateCurrentMoveTo(double angle) {
			if (queuePos >= queueLen || moveQueue[queuePos].doNothing)
				return;

			setAngle(angle, moveQueue[queuePos].rotateOnStart, moveQueue[queuePos].rotatePositive);
		}

		public void completeCurrentAnimation() {
			if (queuePos >= queueLen)
				return;

			if (!moveQueue[queuePos].doNothing) {
				setAngle(90, moveQueue[queuePos].rotateOnStart, moveQueue[queuePos].rotatePositive);
				horizontal = !horizontal;
			}

			if (horizontal) {
				if (currentStart.x < currentEnd.x) {
					baseStart.x = currentStart.x - PADDING_LENGTH;
					baseStart.y = currentStart.y;
					baseEnd.x = currentEnd.x + PADDING_LENGTH;
					baseEnd.y = currentEnd.y;
				} else {
					baseStart.x = currentStart.x + PADDING_LENGTH;
					baseStart.y = currentStart.y;
					baseEnd.x = currentEnd.x - PADDING_LENGTH;
					baseEnd.y = currentEnd.y;
				}
			} else {
				if (currentStart.y < currentEnd.y) {
					baseStart.x = currentStart.x;
					baseStart.y = currentStart.y - PADDING_LENGTH;
					baseEnd.x = currentEnd.x;
					baseEnd.y = currentEnd.y + PADDING_LENGTH;
				} else {
					baseStart.x = currentStart.x;
					baseStart.y = currentStart.y + PADDING_LENGTH;
					baseEnd.x = currentEnd.x;
					baseEnd.y = currentEnd.y - PADDING_LENGTH;
				}

			}

			queuePos++;
		}

		public void completeAllAnimations() {

			for (; queuePos < queueLen;)
				completeCurrentAnimation();

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
		double angle;
		boolean stage1 = true, stage2 = false;

		@Override
		public void run() {

			callcount++;

//			 System.out.println("=== interation ===");
			long curtime = System.currentTimeMillis();
			int timediff = (int) (curtime - time);
			if (timediff >= delay && decrament) {
				fpslabel.setText(Integer.toString(callcount));
				time = curtime;
				callcount = 0;
				timediff = 0;
				stage1 = true;
				stage2 = false;
//				 System.out.println("============ RESET ============");

				myTime.decrement();
				decrament = false;
			}
			
			if (timediff <= (delay / 3.0d)) {
				stage1 = true;
				// double angle = Math.toRadians(timediff * (90.0d / (delay / 2.0d)));
				angle = timediff * (90.0d / (delay / 3.0d));

				sin = Math.sin(angle);
				cos = Math.cos(angle);
			} else if (timediff <= (2.0d*delay / 3.0d)){
				stage2 = true;
				// double angle = Math.toRadians((timediff - delay / 2) * (90.0d / (delay / 2.0d)));
				angle = (timediff - delay / 3.0d) * (90.0d / (delay / 3.0d));
				sin = Math.sin(angle);
				cos = Math.cos(angle);
			}
			else {
				stage1 = false;
				stage2 = false;
				for (Segment segment : ll)
					segment.completeCurrentAnimation();
			}

			if (stage1 && stage2) {
				for (Segment segment : ll)
					segment.completeCurrentAnimation();
				stage1 = false;
			}

			context.clearRect(0, 0, canvas.getCanvasElement().getWidth(), canvas.getCanvasElement().getWidth());

			// Color c = Color.BLACK;
			context.drawImage(dotIcon, 195, 45);
			context.drawImage(dotIcon, 195, 70);

			context.drawImage(dotIcon, 345, 45);
			context.drawImage(dotIcon, 345, 70);

			context.drawImage(dotIcon, 495, 45);
			context.drawImage(dotIcon, 495, 70);

			
			for (Segment segment : ll) {
				if (!segment.draw)
					continue;

				// if (c == Color.BLACK)
				// c = Color.BLUE;
				// else if (c == Color.BLUE)
				// c = Color.CYAN;
				// else if (c == Color.CYAN)
				// c = Color.DARK_ORANGE;
				// else if (c == Color.DARK_ORANGE)
				// c = Color.GREEN;
				// else if (c == Color.GREEN)
				// c = Color.GREY;
				// else if (c == Color.GREY)
				// c = Color.PINK;
				// else if (c == Color.PINK)
				// c = Color.RED;
				// else if (c == Color.RED)
				// c = Color.YELLOW;
				// else if (c == Color.YELLOW)
				// c = Color.BLACK;

				// canvas.setStrokeStyle(c);

				segment.animateCurrentMoveTo(angle);

				context.beginPath();
				{
					context.moveTo(segment.currentStart.x, segment.currentStart.y);
					context.lineTo(segment.currentEnd.x, segment.currentEnd.y);
					context.closePath();
				}
				context.stroke();

			}

		}
	}

	// public class Canvas extends GWTCanvas implements HasMouseMoveHandlers {
	//
	// public Canvas() {
	// super();
	// }
	//
	// public Canvas(int coordX, int coordY) {
	// super(coordX, coordY);
	// }
	//
	// public Canvas(int coordX, int coordY, int pixelX, int pixelY) {
	// super(coordX, coordY, pixelX, pixelY);
	// }
	//
	// @Override
	// public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
	// return addDomHandler(handler, MouseMoveEvent.getType());
	// }
	// }
}
