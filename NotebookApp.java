import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

interface NotebookView {
    void displayNotes(List<Note> notes);
    void displayMessage(String message);
}

interface NotebookPresenter {
    void addNote(String date, String content);
    void loadNotes();
    void saveNotes();
    List<Note> getNotes();
    List<Note> getNotesForDay(LocalDateTime date);
    List<Note> getNotesForWeek(LocalDateTime startOfWeek);
}

class Note {
    private LocalDateTime dateTime;
    private String content;

    public Note(LocalDateTime dateTime, String content) {
        this.dateTime = dateTime;
        this.content = content;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dateTime.format(formatter) + ": " + content;
    }
}

class Notebook {
    private List<Note> notes = new ArrayList<>();

    public void add(Note note) {
        notes.add(note);
    }

    public List<Note> getNotes() {
        return notes;
    }

    public List<Note> getNotesForDay(LocalDateTime date) {
        return filterNotes(date, date.plusDays(1));
    }

    public List<Note> getNotesForWeek(LocalDateTime startOfWeek) {
        return filterNotes(startOfWeek, startOfWeek.plusWeeks(1));
    }

    private List<Note> filterNotes(LocalDateTime start, LocalDateTime end) {
        List<Note> filteredNotes = new ArrayList<>();
        for (Note note : notes) {
            if (note.getDateTime().isAfter(start) && note.getDateTime().isBefore(end)) {
                filteredNotes.add(note);
            }
        }
        filteredNotes.sort(Comparator.comparing(Note::getDateTime));
        return filteredNotes;
    }

    public void saveToFile(String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Note note : notes) {
                writer.write(note.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + ";" + note.getContent());
                writer.newLine();
            }
        }
    }

    public void loadFromFile(String filename) throws IOException {
        notes.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    LocalDateTime dateTime = LocalDateTime.parse(parts[0], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    notes.add(new Note(dateTime, parts[1]));
                }
            }
        }
    }
}

class NotebookPresenterImpl implements NotebookPresenter {
    private NotebookView view;
    private Notebook model;

    public NotebookPresenterImpl(NotebookView view, Notebook model) {
        this.view = view;
        this.model = model;
    }

    @Override
    public void addNote(String date, String content) {
        LocalDateTime dateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        model.add(new Note(dateTime, content));
        view.displayMessage("Запись добавлена.");
    }

    @Override
    public void loadNotes() {
        try {
            model.loadFromFile("notes.txt");
            view.displayNotes(model.getNotes());
        } catch (IOException e) {
            view.displayMessage("Ошибка загрузки записей: " + e.getMessage());
        }
    }

    @Override
    public void saveNotes() {
        try {
            model.saveToFile("notes.txt");
            view.displayMessage("Записи сохранены.");
        } catch (IOException e) {
            view.displayMessage("Ошибка сохранения записей: " + e.getMessage());
        }
    }

    @Override
    public List<Note> getNotes() {
        return model.getNotes();
    }

    @Override
    public List<Note> getNotesForDay(LocalDateTime date) {
        return model.getNotesForDay(date);
    }

    @Override
    public List<Note> getNotesForWeek(LocalDateTime startOfWeek) {
        return model.getNotesForWeek(startOfWeek);
    }
}

class ConsoleNotebookView implements NotebookView {
    @Override
    public void displayNotes(List<Note> notes) {
        if (notes.isEmpty()) {
            System.out.println("Нет записей.");
        } else {
            for (Note note : notes) {
                System.out.println(note);
            }
        }
    }

    @Override
    public void displayMessage(String message) {
        System.out.println(message);
    }
}

public class NotebookApp {
    public static void main(String[] args) {
        Notebook model = new Notebook();
        NotebookView view = new ConsoleNotebookView();
        NotebookPresenter presenter = new NotebookPresenterImpl(view, model);

        Scanner scanner = new Scanner(System.in);
        String command;

        while (true) {
            System.out.println("Введите команду (add, load, save, day, week, exit):");
            command = scanner.nextLine();

            switch (command) {
                case "add":
                    System.out.println("Введите дату (например, 2024-09-24 19:00):");
                    String date = scanner.nextLine();
                    System.out.println("Введите содержание записи:");
                    String content = scanner.nextLine();
                    presenter.addNote(date, content);
                    break;
                case "load":
                    presenter.loadNotes();
                    break;
                case "save":
                    presenter.saveNotes();
                    break;
                case "day":
                    System.out.println("Введите дату для поиска (например, 2024-09-24):");
                    LocalDateTime day = LocalDateTime.parse(scanner.nextLine() + " 00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    view.displayNotes(presenter.getNotesForDay(day));
                    break;
                case "week":
                    System.out.println("Введите дату начала недели (например, 2024-09-23):");
                    LocalDateTime weekStart = LocalDateTime.parse(scanner.nextLine() + " 00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    view.displayNotes(presenter.getNotesForWeek(weekStart));
                    break;
                case "exit":
                    System.out.println("Выход из программы.");
                    scanner.close();
                    return;
                default:
                    System.out.println("Неизвестная команда.");
            }
        }
    }
}
