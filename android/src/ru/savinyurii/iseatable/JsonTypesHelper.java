package ru.savinyurii.iseatable;

public class JsonTypesHelper {
	
	public static class Faculty {
        public final int id;
        public final String name;

        private Faculty(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
	
	public static class Group {
        public final int id;
        public final String name;
        public final int faculty_id;

        private Group(int id, String name, int faculty_id) {
            this.id = id;
            this.name = name;
            this.faculty_id = faculty_id;
        }
    }
}
