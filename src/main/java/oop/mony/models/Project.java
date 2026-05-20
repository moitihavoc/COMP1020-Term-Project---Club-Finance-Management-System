package oop.mony.models;

public class Project {
    private final int projectId;
    private final int userId;
    private final String projectName;

    public Project(String projectName) {
        this(0, 0, projectName);
    }

    public Project(int userId, String projectName) {
        this(0, userId, projectName);
    }

    public Project(int projectId, int userId, String projectName) {
        this.projectId = projectId;
        this.userId = userId;
        this.projectName = projectName == null ? "" : projectName.trim();
    }

    public int getProjectId() {
        return projectId;
    }

    public int getUserId() {
        return userId;
    }

    public String getProjectName() {
        return projectName;
    }

    public boolean hasName() {
        return !projectName.isEmpty();
    }
}
