import java.util.*;

public class CellularAutomations {
    static class Point {
        float x;
        float y;
        Object data;
        int liveNeighbors = 0;

        public String toString() {
            return String.format("(%.3f,%.3f)", this.x, this.y);
        }

        Point(float x, float y, Object data) {
            this.x = x;
            this.y = y;
            this.data = data;
        }
    }

    static class Cell {
        boolean isAlive = false;
        int liveNeighbors = 0;

        Cell(boolean isAlive) {
            this.isAlive = isAlive;
        }
    }

    static class Boundary {
        Point center;
        float halfWidth;
        float halfHeight;

        public String toString() {
            return String.format("x:(%.3f,%.3f) y:(%.3f,%.3f)", this.center.x - halfWidth, this.center.x + halfWidth,
                    this.center.y - halfHeight, this.center.y + halfHeight);
        }

        Boundary(Point center, float halfWidth, float halfHeight) {
            this.center = center;
            this.halfWidth = halfWidth;
            this.halfHeight = halfHeight;
        }

        boolean contains(Point p) {
            return (this.center.x - halfWidth <= p.x && this.center.x + halfWidth >= p.x
                    && this.center.y - halfHeight <= p.y && this.center.y + halfHeight >= p.y);
        }

        boolean intersects(Boundary other) {
            if (this.center.x - this.halfWidth > other.center.x + other.halfWidth ||
                    this.center.x + this.halfWidth < other.center.x - other.halfWidth ||
                    this.center.y - this.halfHeight > other.center.y + other.halfHeight ||
                    this.center.y + this.halfHeight < other.center.y - other.halfHeight)
                return false;
            return true;
        }
    }

    public static void main(String[] args) {

    }

    static void updateNeighbors(QuadTree node) {
        if (node == null)
            return;
        for (Point point : node.points) {
            Boundary boundary = new Boundary(point, 1.5f, 1.5f);
            ArrayList<Point> neighbors = node.query(boundary);
            int liveNeighbors = 0;
            for (Point neighbor : neighbors) {
                Cell cell = (Cell) neighbor.data;
                if (cell.isAlive)
                    liveNeighbors++;
            }
            Cell cell = (Cell) point.data;
            cell.liveNeighbors = liveNeighbors;
            point.data = cell;
        }
        if (node.divided) {
            updateNeighbors(node.northeast);
            updateNeighbors(node.northwest);
            updateNeighbors(node.southeast);
            updateNeighbors(node.southwest);
        }
    }

    static void updateState(QuadTree node) {
        if (node == null)
            return;
        for (Point point : node.points) {
            Cell cell = (Cell) point.data;
            if (cell.isAlive) {
                if (cell.liveNeighbors < 2 || cell.liveNeighbors > 3)
                    cell.isAlive = false;
            } else if (cell.liveNeighbors == 3)
                cell.isAlive = true;
            point.data = cell;
        }
        if (node.divided) {
            updateState(node.northeast);
            updateState(node.northwest);
            updateState(node.southeast);
            updateState(node.southwest);
        }
    }

    static class QuadTree {
        ArrayList<Point> points;
        int capacity;
        Boundary boundary;
        QuadTree northwest;
        QuadTree northeast;
        QuadTree southwest;
        QuadTree southeast;
        int generation = 0;
        int liveCells = 0;

        QuadTree(Boundary boundary, int capacity) {
            this.boundary = boundary;
            this.capacity = capacity;
            this.points = new ArrayList<Point>(capacity);
            this.northwest = this.southeast = this.southwest = this.northeast = null;
        }

        void subdivide() {
            float w = this.boundary.halfWidth / 2;
            float h = this.boundary.halfHeight / 2;
            Point center = this.boundary.center;
            Object data = null;
            this.northwest = new QuadTree(new Boundary(new Point(center.x - w, center.y + h, data), w, h),
                    this.capacity);
            this.northeast = new QuadTree(new Boundary(new Point(center.x + w, center.y + h, data), w, h),
                    this.capacity);
            this.southwest = new QuadTree(new Boundary(new Point(center.x - w, center.y - h, data), w, h),
                    this.capacity);
            this.southeast = new QuadTree(new Boundary(new Point(center.x + w, center.y - h, data), w, h),
                    this.capacity);
            this.divided = true;
        }

        boolean divided = false;

        boolean insert(Point p) {
            if (!this.boundary.contains(p))
                return false;
            if (this.points.size() < this.capacity) {
                this.points.add(p);
                return true;
            } else if (!this.divided) {
                this.subdivide();
            }
            if (this.northwest.insert(p))
                return true;
            else if (this.northeast.insert(p))
                return true;
            else if (this.southwest.insert(p))
                return true;
            else if (this.southeast.insert(p))
                return true;
            return false;
        }

        ArrayList<Point> query(Boundary range) {
            ArrayList<Point> found = new ArrayList<Point>();
            if (!this.boundary.intersects(range)) {
                return found;
            }
            for (Point point : this.points)
                if (range.contains(point))
                    found.add(point);
            if (this.divided) {
                found.addAll(this.northwest.query(range));
                found.addAll(this.northeast.query(range));
                found.addAll(this.southeast.query(range));
                found.addAll(this.southwest.query(range));
            }
            return found;
        }

    }
}
