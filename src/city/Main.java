package city;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.Timer;

public class Main extends Canvas implements ActionListener{

	Timer tm = new Timer(200, this);

	private final static int traffic_light_delay = 13;
	private final static int cycle_length = 120;
	private final static double limit_velocity = 8.3333333333;
	private final static int [] x_coord = {
			251, 375, 589, 292, 390, 335, 364, 397, 376, 401, 443, 449, 506, 454, 523, 519, 338, 382, 412, 343, 391, 420, 260, 296, 315, 375, 397, 429, 230, 282, 336, 382, 410, 439, 154, 242, 276, 296, 347, 449, 125, 227, 142, 229, 296, 360, 298, 345, 297, 345, 185, 224, 178, 228, 235, 328, 320, 365, 267, 339, 373, 381, 441, 443, 452, 451, 449, 474, 487, 492, 503, 512, 511, 529, 537, 554, 503, 483, 576, 581, 626, 638, 670, 687, 666, 667, 579, 597, 645, 667, 681, 751, 763, 759, 749, 745, 741, 659, 665, 738, 640, 666, 640, 666, 689, 730, 727, 737, 755, 772, 786, 800, 822, 826, 837, 859, 880, 925, 872, 923, 982, 1018, 1035, 1051, 942, 1001, 945, 992, 840, 773, 827, 844, 843, 877, 905, 910, 909, 917, 862, 887, 960, 1030, 1043, 1109, 1115, 1120, 
	};
	private final static int [] y_coord = {
			148, 92, 117, 213, 149, 202, 189, 180, 214, 212, 170, 210, 195, 305, 289, 223, 270, 265, 262, 319, 310, 309, 306, 334, 343, 357, 352, 344, 354, 370, 366, 394, 395, 381, 441, 435, 413, 454, 447, 434, 480, 494, 521, 517, 498, 484, 541, 540, 594, 600, 615, 615, 679, 648, 685, 690, 717, 712, 828, 831, 674, 636, 595, 636, 706, 748, 800, 536, 601, 637, 727, 764, 816, 604, 726, 819, 552, 443, 212, 265, 203, 266, 205, 255, 308, 362, 599, 672, 572, 656, 562, 299, 332, 366, 468, 556, 655, 717, 811, 823, 88, 66, 147, 110, 95, 215, 162, 133, 180, 153, 252, 222, 194, 338, 286, 254, 208, 155, 303, 220, 152, 222, 289, 340, 410, 387, 477, 479, 561, 666, 830, 620, 665, 726, 622, 665, 720, 753, 830, 839, 810, 582, 767, 629, 714, 749, 
	};
	static ArrayList<Car> drawn_cars = new ArrayList<Car>();
	static int [][] graph;

	static int point_a = -1;
	static int point_b = -1;

	static class Path {
		int distance;
		int node;
		String indicator;
		Path(int d, int n, String id) {
			distance = d;
			node = n;
			indicator = id;
		}
	}

	static class TrafficLight{
		int cycle;
		int node;
		int power;
		TrafficLight(int c, int n) {
			cycle = c;
			node = n;
			power = c % (cycle_length*2) < cycle_length ? 0:1;
		}
	}

	static class Congestion{
		private int total;
		private LinkedList<Integer> route;
		private LinkedList<Integer> distance;
		Congestion(int t, LinkedList<Integer> r, LinkedList<Integer> d) {
			total = t;
			route = r;
			distance = d;
		}
		public Congestion clone() {
			return new Congestion(total, (LinkedList<Integer>) route.clone(), (LinkedList<Integer>) distance.clone());
		}
		public int getTotal() {
			return total;
		}
	}

	static class Car{
		int travelled;
		Congestion congest;
		int x;
		int y;
		private double curr_x;
		private double curr_y;
		double dx;
		double dy;
		private Color [] clr = {Color.RED, Color.BLUE, Color.BLACK, Color.GRAY, Color.WHITE};
		Color color;
		Car(Congestion c, int cx, int cy) {
			color = clr[(int) (Math.random()*(clr.length-1))];
			congest = c.clone();
			curr_x = cx;
			curr_y = cy;
			travelled = congest.route.size() - 1;
			if (travelled>=0) {
				reset();
			}
		}
		private void reset() {
			x = x_coord[congest.route.get(travelled)];
			y = y_coord[congest.route.get(travelled)];
			double theta = Math.atan(((double)y - curr_y)/((double)x - curr_x));
			if (x - curr_x >= 0 && y - curr_y < 0 || x - curr_x >= 0 && y - curr_y > 0) {
				theta = 0 + theta;
			}
			else if (x - curr_x <= 0 && y - curr_y < 0 || x - curr_x < 0 && y - curr_y >= 0) {
				theta = Math.PI + theta;
			}
			double hypoteneuse = Math.sqrt(Math.pow(x - curr_x, 2) + Math.pow(y - curr_y, 2))*5/congest.distance.get(travelled);
			dx = hypoteneuse*Math.cos(theta);
			dy = hypoteneuse*Math.sin(theta);
		}
		public boolean move() {
			curr_x += dx;
			curr_y += dy;

			if (x <= curr_x + limit_velocity && x >= curr_x - limit_velocity && y <= curr_y + limit_velocity && y >= curr_y - limit_velocity) {
				curr_x = x;
				curr_y = y;
				travelled--;
				if (travelled>=0) {
					reset();
				}
			}
			if (travelled>=0) {
				return true;
			}
			return false;
		}
		public int getCurrentX() {
			return (int)curr_x;
		}
		public int getCurrentY() {
			return (int)curr_y;
		}
		public Congestion getCongestion() {
			return congest;
		}
		public Color getColor() {
			return color;
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("City");
		Canvas canvas = new Main();
		canvas.setSize(1600, 1600);
		frame.add(canvas);
		frame.pack();
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.out.println("Terminated");
				System.exit(0);
			}
		});

		FastReader sc = new FastReader();

		String fileName = "tokyo.txt";

		LinkedList<Path> queue = new LinkedList<Path>();
		ArrayList<TrafficLight> traffic = new ArrayList<TrafficLight>();

		String line = null;

		try {
			FileReader fileReader = new FileReader(fileName.trim());
			BufferedReader in = new BufferedReader(fileReader);
			int c = 0;
			graph = new int [200][200];
			while((line = in.readLine()) != null) {
				String[] arr = line.trim().split(",");
				int node = Integer.parseInt(arr[0].split(" ")[0]);
				if (arr[0].split(" ").length > 1 && arr[0].split(" ")[1]=="&") {
					traffic.add(new TrafficLight((int)(Math.random()*(cycle_length-1)), node));
				}
				String[] arr2 = arr[1].trim().split(" ");
				for (int i=0; i<arr2.length; i++) {
					graph[node][Integer.parseInt(arr2[i].split("-")[0])] = 
							Integer.parseInt(arr2[i].split("-")[1]);
				}
				c++;
			}   
			in.close();

			int [] dist = new int [c];
			int [][] orig_graph = graph.clone();

			int start_time = (int)(System.currentTimeMillis() / 1000L);

			canvas.addMouseListener(new MouseAdapter() {
				boolean select_flag = false;

				public void mousePressed(MouseEvent e) {
					int mouse_x = e.getX();
					int mouse_y = e.getY();
					point_b = -1;
					for (int i=0; i<x_coord.length; i++) {
						if (x_coord[i]-13<=mouse_x && x_coord[i]+13>=mouse_x &&
								y_coord[i]-13<=mouse_y && y_coord[i]+13>=mouse_y) {
							point_b = i;
							break;
						}
					}

					if (!select_flag && point_b != -1) {
						point_a = point_b;
						point_b = -1;
						select_flag = true;
					} else {
						setRoute(point_a, point_b, queue, dist, traffic, start_time, orig_graph);
						point_a = -1;
						point_b = -1;
						select_flag = false;
					}

				}
			});

			while (true) {
				int a = (int) (Math.random()*145);
				int n = (int) (Math.random()*145);
				setRoute(a, n, queue, dist, traffic, start_time, orig_graph);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		catch(FileNotFoundException ex) {
			System.out.println("Unable to open file '" + fileName + "'");    
		}
		catch(IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");                  
		}

	}

	public static void setRoute(int a, int n, LinkedList<Path> queue, int [] dist, ArrayList<TrafficLight> traffic, int start_time, int [][] orig_graph) {
		Arrays.fill(dist, Integer.MAX_VALUE);
		queue.clear();
		System.out.println("Departing Location: Block No. "+a);
		System.out.println("Destination: Block No. "+n);
		queue.add(new Path(0, a, ""));
		setTrafficLights(traffic, start_time, orig_graph);
		Congestion vehicle = dijkstra(n, queue, dist);
		drawn_cars.add(new Car(vehicle, x_coord[a], y_coord[a]));
		System.out.println(vehicle.total/1000+" km, "+vehicle.total%1000+" m");
		System.out.println(distanceToTime(vehicle.total, limit_velocity));
		System.out.print(a);
		for (int i=0; i<vehicle.route.size(); i++) {
			System.out.print(" --> "+vehicle.route.get(vehicle.route.size()-i-1));
			System.out.print(" ("+vehicle.distance.get(vehicle.distance.size()-i-1)+" m)");					
		}
		System.out.println();
	}

	private static String distanceToTime(int d, double v) {
		if (d == -1) {
			return "Route does not exist";
		}
		return Integer.toString((int)((d/v)/60))+" min, "+Integer.toString((int)(d/v)%60)+" sec";
	}

	private static void setTrafficLights(ArrayList<TrafficLight> traffic, int start_time, int [][] orig_graph) {
		int curr_time = (int)(System.currentTimeMillis() / 1000L);
		for (int i=0; i<traffic.size(); i++) {
			boolean flag = traffic.get(i).power != ((traffic.get(i).cycle +
					curr_time - start_time) % (cycle_length*2) < cycle_length ? 0:1);
			traffic.set(i, new TrafficLight(traffic.get(i).cycle +
					curr_time - start_time, traffic.get(i).node));
			if (flag) {
				for (int j=0; j<graph[traffic.get(i).node].length; j++) {
					if (graph[traffic.get(i).node][i]>0) {
						graph[traffic.get(i).node][i] = orig_graph[traffic.get(i).node][i] +
								((traffic.get(i).cycle % (cycle_length*2) - cycle_length)*traffic_light_delay);
						if (graph[traffic.get(i).node][i]<=0) {
							graph[traffic.get(i).node][i] = 1;
						}
					}
					if (graph[i][traffic.get(i).node]>0) {
						graph[i][traffic.get(i).node] = orig_graph[i][traffic.get(i).node] -
								(traffic.get(i).cycle % (cycle_length*2) - cycle_length)*traffic_light_delay;
						if (graph[i][traffic.get(i).node]<=0) {
							graph[i][traffic.get(i).node] = 1;
						}
					}
				}
			}
		}
	}

	private static Congestion dijkstra(int n, LinkedList<Path> queue, int [] dist) {
		TreeMap<String, Integer> traceback = new TreeMap<>();
		TreeMap<String, Integer> weightback = new TreeMap<>();
		int index = 0;
		while(!queue.isEmpty()) {
			Path P = queue.pop();
			int N = P.node;
			int D = P.distance;
			String ID = P.indicator;
			if (D<dist[N]) {
				dist[N] = D;
				for (int i=0; i<graph[N].length; i++) {
					if (graph[N][i]>0) {
						queue.add(new Path(D+graph[N][i], i, ID+Character.toString((char)((index+32)%127))));
						traceback.put(ID+Character.toString((char)((index+32)%127)), i);
						weightback.put(ID+Character.toString((char)((index+32)%127)), graph[N][i]);
						index++;
					}
				}
				if (N == n) {
					LinkedList<Integer> route = new LinkedList<Integer>();
					LinkedList<Integer> distance = new LinkedList<Integer>();
					int subsize = ID.length();
					while (subsize>0) {
						if (traceback.containsKey(ID.substring(0, subsize))) {
							route.add(traceback.get(ID.substring(0, subsize)));
							distance.add(weightback.get(ID.substring(0, subsize)));
						}
						subsize--;
					}
					return new Congestion(D, route, distance);
				}
			}
		}
		return new Congestion(-1, new LinkedList<Integer>(), new LinkedList<Integer>());
	}

	public void paint(Graphics g) {
		Toolkit t = Toolkit.getDefaultToolkit();  

		Image map = t.getImage("map_visual.jpg");  
		g.drawImage(map, 0, 0,this);  

		g.setColor(Color.GREEN);

		for (int i=0; i<x_coord.length; i++) {
			g.drawOval(x_coord[i]-13, y_coord[i]-13, 25, 25);
		}

		for (int i=0; i<graph.length; i++) {
			for (int j=0; j<graph[i].length; j++) {
				if (graph[i][j]>0) {
					g.drawLine(x_coord[i], y_coord[i], x_coord[j], y_coord[j]);
				}
			}
		}

		g.setColor(Color.RED);
		if (point_a != -1) {
			g.drawOval(x_coord[point_a]-13, y_coord[point_a]-13, 25, 25);
		}

		for (int car=0; car<drawn_cars.size(); car++) {
			g.setColor(drawn_cars.get(car).getColor());
			g.fillOval(drawn_cars.get(car).getCurrentX()-7, drawn_cars.get(car).getCurrentY()-7, 15, 15);
		}

		tm.start();
	}

	public void actionPerformed(ActionEvent e) {
		for (int car=0; car<drawn_cars.size(); car++) {
			if (!drawn_cars.get(car).move()) {
				drawn_cars.remove(car);
				car--;
			}
		}
		repaint();
		tm.restart();
	}

	static class FastReader {
		BufferedReader br;
		StringTokenizer st;
		public FastReader() {
			br = new BufferedReader(new
					InputStreamReader(System.in));
		}

		String next() {
			while (st == null || !st.hasMoreElements()) {
				try {
					st = new StringTokenizer(br.readLine());
				}
				catch (IOException  e)
				{
					e.printStackTrace();
				}
			}
			return st.nextToken();
		}

		int nextInt() {
			return Integer.parseInt(next());
		}

		long nextLong() {
			return Long.parseLong(next());
		}

		double nextDouble() {
			return Double.parseDouble(next());
		}

		String nextLine() {
			String str = "";
			try {
				str = br.readLine();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			return str;
		}
	}

}