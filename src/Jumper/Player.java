package Jumper;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.Timer;

public class Player implements ActionListener, MouseListener, KeyListener {

	public static Player flp;
	public final int WIDTH = 1200, HEIGHT = 800;
	public Renderer renderer;
	public Rectangle bird;
	public BufferedImage image;
	public JFrame frame;

	public final String TITLE = "Jumper";
	public static final int GAME_SPEED = 5;
	public int ticks=0, yMotion=0, score = 0, highScore,i=0;

	public boolean gameOver, started;

	public ArrayList<Rectangle> columns;
	public Random rand;
	long lastTime = System.nanoTime(), timer = System.currentTimeMillis();
	final double ns = 1000000000.0/60.0;
	double delta = 0;
	int frames = 0;
	int updates = 0;

	public Player() {

		frame = new JFrame();
		Timer timer = new Timer(20, this);
		try {
			image = ImageIO.read(getClass().getResourceAsStream("/Bird.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		renderer = new Renderer();
		rand = new Random();

		frame.add(renderer);
		frame.setSize(WIDTH, HEIGHT);
		frame.setTitle("Flappy");
		frame.addMouseListener(this);
		frame.addKeyListener(this);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		bird = new Rectangle(WIDTH / 2 - 10, HEIGHT / 2 - 10, 20, 20);
		columns = new ArrayList<Rectangle>();

		addColumn(true);
		addColumn(true);
		addColumn(true);
		addColumn(true);

		timer.start();
	}

	public void addColumn(boolean start) {
		int space = 300;
		int width = 100;
		int height = 50 + rand.nextInt(300);
		if (start) {
			// System.out.println(columns.size());
			columns.add(new Rectangle(WIDTH + width + columns.size() * 300,
					HEIGHT - height - 120, width, height));
			columns.add(new Rectangle(WIDTH + width + (columns.size() - 1)
					* 300, 0, width, HEIGHT - height - space));
		}
		//	else {
		//	columns.add(new Rectangle(columns.get(columns.size() - 1).x + 600,
		//			HEIGHT - height - 120, width, height));
		//	columns.add(new Rectangle(columns.get(columns.size() - 1).x, 0,
		//			width, HEIGHT - height - space));
		//}

	}

	public void paintColumn(Graphics g, Rectangle column) {
		g.setColor(Color.blue);
		g.fillRect(column.x, column.y, column.width, column.height);
	}

	public void jump() {
		if (gameOver) {
			bird = new Rectangle(WIDTH / 2 - 10, HEIGHT / 2 - 10, 20, 20);
			columns.clear();
			yMotion = 0;
			score = 0;
			addColumn(true);
			addColumn(true);
			addColumn(true);
			addColumn(true);

			gameOver = false;
		}

		if (!started) {
			started = true;
		} else if (!gameOver) {
			if (yMotion > 0) {
				yMotion = 0;
			}

			yMotion -= 10;
		}
	}
	public void actionPerformed(ActionEvent arg0) {
		long now = System.nanoTime();
		delta +=(now - lastTime)/ns;
		lastTime = now; 
		while(delta>=1) {
			updates++;
			update();
			delta--;
		}
		frames++;
		renderer.repaint();
		if(System.currentTimeMillis()-timer > 1000) {
			timer +=1000;
			System.out.println("Updates = "+updates+"ups");
			System.out.println("Frames = "+frames+"fps");
			frame.setTitle(TITLE+"  |  "+ frames + "fps");
			frames = updates = 0;
		}

	}
	public void update() {

		ticks++;
		
		if (started) {
			// System.out.println(ticks +"Columns Size"+columns.size());
			int speed = GAME_SPEED;

			for (int i = 0; i < columns.size(); i++) {
				Rectangle column = columns.get(i);
				column.x -= speed;
			}

			if (ticks % 2 == 0 && yMotion < 15) {
				yMotion += 2;
			}

			for (int i = 0; i < columns.size(); i++) {
				Rectangle column = columns.get(i);

				if (column.x + column.width < 0) {
					// System.out.println("column.x + column.width < 0"+(column.x
					// + column.width < 0));
					columns.remove(column);

					if (column.y == 0) {
						// System.out.println("column.y==0");
						addColumn(false);
					}
				}
			}

			bird.y += yMotion;

			for (Rectangle column : columns) {
				//Score calculator
				/*if (column.y == 0
						&& (bird.x + bird.width / 2 > column.x + column.width
								/ 2 - 10)
						&& (bird.x + bird.width / 2 < column.x + column.width
								/ 2 + 10)) {
					score++;
				}*/
				if(column.y==0 && bird.x==column.x+100){
					score++;
				}

				if (column.intersects(bird)) {
					gameOver = true;

					if (bird.x <= column.x) {
						bird.x = column.x - bird.width;
					} 
					else {
						if (column.y != 0) {
							bird.y = column.y - bird.height;
						}
						else if (bird.y < column.height) {
							bird.y = column.height;
						}
					}

					bird.x = column.x - bird.width;
				}

			}

			if (bird.y > HEIGHT - 120 - bird.height || bird.y < 0) {
				gameOver = true;
			}
			if (bird.y + yMotion >= HEIGHT - 120) {
				bird.y = HEIGHT - 120 - bird.height;
			}

		}

	}

	public void repaint(Graphics g) {

		g.setColor(Color.gray);
		g.fillRect(0, 0, WIDTH, HEIGHT);

		g.setColor(Color.orange);
		g.fillRect(0, HEIGHT - 120, WIDTH, 120);

		g.setColor(Color.green.darker().darker());
		g.fillRect(0, HEIGHT - 120, WIDTH, 20);

		g.setColor(Color.gray);
		g.fillRect(bird.x, bird.y, bird.width, bird.height);

		// Drawing Image
		g.drawImage(image, bird.x, bird.y, null);

		for (Rectangle column : columns) {
			paintColumn(g, column);
		}

		g.setColor(Color.white);
		g.setFont(new Font("Calibri", 1, 80));

		if (!started) {
			g.drawString("Click to Start!", 75, HEIGHT / 2 - 50);
		}

		if (gameOver) {
			g.drawString("GameOver!!", 100, HEIGHT / 2 - 50);
			g.drawString("HIGH SCORE : ", 100, 500);
			g.drawString(String.valueOf(highScore), 750, 500);
			g.drawString("YOUR SCORE : ", 100, 650);
			g.drawString(String.valueOf(score), 760, 650);
			if (highScore < score) {
				highScore = score;
			}
		}

		if (!gameOver && started) {
			g.drawString(String.valueOf(score), WIDTH / 2 - 25, 100);
			g.setFont(new Font("TimesNewRoman", 1, 20));
			g.drawString("HIGH SCORE : ", 700, 50);
			g.drawString(String.valueOf(highScore), 900, 50);
		}

	}

	public static void main(String args[]) {

		flp = new Player();

	}

	public void mouseClicked(MouseEvent arg0) {
		jump();

	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			jump();
		}

	}

	public void mouseEntered(MouseEvent arg0) {

	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void keyPressed(KeyEvent e) {
//		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
//			jump();
//		}
	}

	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

}
