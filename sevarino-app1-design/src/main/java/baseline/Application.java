package baseline;

import java.io.IOException;
import java.text.ParseException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Application {

        public static void main(String[] ignored) throws ParseException, IOException, ClassNotFoundException {
            Tree tree = new Tree("/tmp/todos.tree.db");

            System.out.println("What do you want to do?\n");
            while(true){
                System.out.printf("> ");
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String argsString = reader.readLine();
                String[] args = argsString.split(" ");

                if (args.length < 1){
                    System.out.printf("error");
                    return;
                }
                String firstArg = args[0];

                if ("add_container".equals(firstArg)) {
// add_container "id" "title"
                    if (args.length < 3){
                        System.out.printf("needs id and title");
                        continue;
                    }
                    int id = Integer.parseInt(args[1]);
                    String title = args[2];
                    tree.findAndAddTodo(new Container(title), id);
                    System.out.println("Done adding a container : " + title + " to id: " + id);
                } else if ("add_item".equals(firstArg)) {
// add_item "id" "title"
                    if (args.length < 3){
                        System.out.printf("Add an item requires a container id and a title");
                        continue;
                    }
                    int id = Integer.parseInt(args[1]);
                    String title = args[2];
                    tree.findAndAddTodo(new Item(title), id);
                    System.out.println("Done adding item: " + title + " to id: " + id);
                } else if ("delete".equals(firstArg)){
// delete "id"
                    if (args.length < 2){
                        System.out.printf("Delete a container requires a container id");
                        continue;
                    }
                    int id = Integer.parseInt(args[1]);
                    tree.findAndDeleteTodo(id);
                    System.out.println("Done deleting a todo id: " + id);


        public static class Tree implements Serializable {

            private String saveLocation = "/tmp/todos.db";
            private Container root = new Container("");

            public Tree(String saveLocation){
                this.saveLocation = saveLocation;
            }

            public Tree findAndAddTodo(TodoAbstract todo, int id){
                if (id == 0){
                    root.add(todo);
                } else {
                    root.findAndAddContainer(todo, id);
                }

                return this;
            }

            public Tree findAndDeleteTodo(int id){
                if (id == 0){
                    System.out.printf("Can't delete the root");
                    return this;
                } else {
                    root.findAndDeleteTodo(id);
                }

                return this;
            }

            public String toString(){
                return root.toString();
            }

            public String printTree(){
                return recursivePrint(root, 0);
            }

            private String recursivePrint(TodoAbstract node, int level){
                String result = "";
                String indent = "";
                for(int i = 0; i < level; i++){
                    indent += " ";
                }

                result += indent + "-" + node.getClass().getSimpleName() + "[id]=" + node.id + "\n";
                result += indent + "(todo): " + node.title + "\n";

                if (node.getClass() == Container.class){
                    for (Map.Entry<Integer, TodoAbstract> n : ((Container) node).todos.entrySet()){
                        result += recursivePrint(n.getValue(), level + 1);
                    }
                }

                return result;
            }

            public Tree load() throws IOException, ClassNotFoundException {
                Path path = Paths.get(saveLocation);
                byte[] array = Files.readAllBytes(path);
                ByteArrayInputStream bis = new ByteArrayInputStream(array);
                ObjectInput in = null;

                try {
                    in = new ObjectInputStream(bis);
                    return (Tree) in.readObject();
                } finally {
                    try {
                        bis.close();
                    } catch (IOException ex) {
// ignore close exception
                    }
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (IOException ex) {
// ignore close exception
                    }
                }
            }

            public void save() throws IOException {
                OutputStream file = new FileOutputStream(saveLocation);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutput out = null;
                try {
                    out = new ObjectOutputStream(bos);
                    out.writeObject(this);
                    file.write(bos.toByteArray());
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException ex) {
// ignore close exception
                    }
                    try {
                        bos.close();
                    } catch (IOException ex) {
// ignore close exception
                    }
                }
                file.close();
            }
        }

        public static class Container extends TodoAbstract {
            public Map<Integer, TodoAbstract> todos = new HashMap<>();

            public Container(String title){
                super(title);
            }

            public Container add(TodoAbstract todo){
                todos.put(todo.id, todo);
                return this;
            }

            public Container delete(int id){
                todos.remove(id);
                return this;
            }

            public Container findAndAddContainer(TodoAbstract todo, int id){
                if (this.id != id){
                    for(Map.Entry<Integer, TodoAbstract> n : todos.entrySet()){
                        if (n.getValue().getClass() == Container.class){
                            ((Container)n.getValue()).findAndAddContainer(todo, id);
                        }
                    }
                } else {
                    add(todo);
                }

                return this;
            }

            public Container findAndDeleteTodo(int id) {
                if (todos.containsKey(id)) {
                    todos.remove(id);
                } else {
                    for (Map.Entry<Integer, TodoAbstract> n : todos.entrySet()) {
                        if (n.getValue().getClass() == Container.class) {
                            ((Container) n.getValue()).findAndDeleteTodo(id);
                        }
                    }
                }

                return this;
            }

            public String toString(){
                return todos.toString();
            }
        }

        public static class Item extends TodoAbstract {
            public Item(String title) {
                super(title);
            }
        }

        public static abstract class TodoAbstract implements Todo, Serializable {
            protected final int id;
            protected String title;

            public TodoAbstract(String title){
                this.id = UUID.randomUUID().hashCode();
                this.title = title;
            }

            public String toString(){
                return "{ type: " + getClass().getSimpleName() +
                        ", id: " + id +
                        ", title: " + title + "}";
            }
        }

        interface Todo {

        }
    }

