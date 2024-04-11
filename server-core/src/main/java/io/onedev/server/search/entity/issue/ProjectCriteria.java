package io.onedev.server.search.entity.issue;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.commons.utils.match.WildcardUtils;

import javax.persistence.criteria.*;

public class ProjectCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;
	
	private String projectPath;
	
	private final int operator;

	public ProjectCriteria(String projectPath, int operator) {
		this.projectPath = projectPath;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		var predicate =  OneDev.getInstance(ProjectManager.class).getPathMatchPredicate(
				builder, from.join(Issue.PROP_PROJECT, JoinType.INNER), projectPath);
		if (operator == IssueQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Issue issue) {
		var matches = WildcardUtils.matchPath(projectPath, issue.getProject().getPath());
		if (operator == IssueQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public void onMoveProject(String oldPath, String newPath) {
		projectPath = Project.substitutePath(projectPath, oldPath, newPath);
	}

	@Override
	public boolean isUsingProject(String projectPath) {
		return Project.containsPath(this.projectPath, projectPath);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Issue.NAME_PROJECT) + " " 
				+ IssueQuery.getRuleName(operator) + " " 
				+ quote(projectPath);
	}

}
