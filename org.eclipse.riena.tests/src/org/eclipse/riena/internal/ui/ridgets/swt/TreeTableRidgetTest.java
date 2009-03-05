/*******************************************************************************
 * Copyright (c) 2007, 2009 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.internal.ui.ridgets.swt;

import java.beans.PropertyChangeEvent;
import java.util.Comparator;

import org.eclipse.riena.beans.common.Person;
import org.eclipse.riena.beans.common.TypedComparator;
import org.eclipse.riena.beans.common.WordNode;
import org.eclipse.riena.tests.TreeUtils;
import org.eclipse.riena.tests.UITestHelper;
import org.eclipse.riena.ui.common.ISortableByColumn;
import org.eclipse.riena.ui.ridgets.IColumnFormatter;
import org.eclipse.riena.ui.ridgets.IGroupedTreeTableRidget;
import org.eclipse.riena.ui.ridgets.IRidget;
import org.eclipse.riena.ui.ridgets.ITreeTableRidget;
import org.eclipse.riena.ui.ridgets.ISelectableRidget.SelectionType;
import org.eclipse.riena.ui.ridgets.swt.ColumnFormatter;
import org.eclipse.riena.ui.ridgets.swt.uibinding.DefaultSwtControlRidgetMapper;
import org.eclipse.riena.ui.ridgets.tree2.TreeNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Tests of the class {@link TreeTableRidget}.
 */
public class TreeTableRidgetTest extends AbstractSWTRidgetTest {

	private PersonNode[] roots;
	private PersonNode node1;
	private PersonNode node2;
	private PersonNode node3;
	private PersonNode node4;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		roots = initializeTreeModel();
		String[] valueAccessors = new String[] { "firstname", "lastname" };
		String[] columnHeaders = new String[] { "First Name", "Last Name" };
		getRidget().bindToModel(roots, PersonNode.class, "children", "parent", valueAccessors, columnHeaders);
	}

	@Override
	protected Control createWidget(Composite parent) {
		Tree result = new Tree(parent, SWT.MULTI);
		result.setHeaderVisible(true);
		new TreeColumn(result, SWT.NONE);
		new TreeColumn(result, SWT.NONE);
		return result;
	}

	@Override
	protected IRidget createRidget() {
		return new TreeTableRidget();
	}

	@Override
	protected Tree getWidget() {
		return (Tree) super.getWidget();
	}

	@Override
	protected TreeTableRidget getRidget() {
		return (TreeTableRidget) super.getRidget();
	}

	// test methods
	// /////////////

	public void testRidgetMapping() {
		DefaultSwtControlRidgetMapper mapper = new DefaultSwtControlRidgetMapper();
		assertSame(TreeTableRidget.class, mapper.getRidgetClass(getWidget()));
	}

	public void testBindToModel() {
		TreeTableRidget ridget = getRidget();
		Tree control = getWidget();

		ridget.expandAll();

		assertEquals(9, TreeUtils.getItemCount(control));
		assertEquals(node1.getFirstname(), getUIControlItem(0).getText(0));
		assertEquals(node2.getFirstname(), getUIControlItem(1).getText(0));
		assertEquals(node3.getFirstname(), getUIControlItem(2).getText(0));
		assertEquals(node4.getFirstname(), getUIControlItem(3).getText(0));

		assertEquals(node1.getLastname(), getUIControlItem(0).getText(1));
		assertEquals(node2.getLastname(), getUIControlItem(1).getText(1));
		assertEquals(node3.getLastname(), getUIControlItem(2).getText(1));
		assertEquals(node4.getLastname(), getUIControlItem(3).getText(1));
	}

	public void testBindToModelTooFewColumns() {
		Tree control = getWidget();

		assertEquals(2, control.getColumnCount());

		// the tree widget has two columns, we bind only one
		try {
			getRidget().bindToModel(roots, PersonNode.class, "children", "parent", new String[] { "firstname" },
					new String[] { "First Name" });
			fail();
		} catch (RuntimeException rex) {
			ok();
		}
	}

	public void testBindToModelWithTooManyColumns() {
		Tree control = getWidget();

		assertEquals(2, control.getColumnCount());

		// the tree widget has two columns but we bind three
		try {
			getRidget().bindToModel(roots, PersonNode.class, "children", "parent",
					new String[] { "firstname", "lastname", "entry" },
					new String[] { "First Name", "Last Name", "First - Last" });
			fail();
		} catch (RuntimeException rex) {
			ok();
		}

		// tree has 1 default column, expected 3
		try {
			getRidget().setUIControl(new Tree(getShell(), SWT.NONE));
			fail();
		} catch (RuntimeException rex) {
			ok();
		}
	}

	public void testTableColumnsNumAndHeader() {
		Tree control = getWidget();

		TreeColumn[] columns = control.getColumns();
		assertEquals(2, columns.length);
		assertEquals("First Name", columns[0].getText());
		assertEquals("Last Name", columns[1].getText());
		assertTrue(control.getHeaderVisible());
	}

	public void testTableColumnsNumAndHeaderWithMismatch() {
		String[] properties1 = new String[] { "firstname", "lastname" };
		String[] headers1 = new String[] { "First Name" };

		try {
			getRidget().bindToModel(roots, PersonNode.class, "children", "parent", properties1, headers1);
			fail();
		} catch (RuntimeException rex) {
			ok();
		}
	}

	public void testTableColumnsWithNullHeader() {
		Tree control = getWidget();

		control.setHeaderVisible(true);
		control.getColumn(0).setText("foo");
		control.getColumn(1).setText("bar");

		String[] properties1 = new String[] { "firstname", "lastname" };
		// null should hide the headers
		getRidget().bindToModel(roots, PersonNode.class, "children", "parent", properties1, null);

		assertFalse(control.getHeaderVisible());
	}

	public void testTableColumnsWithNullHeaderEntry() {
		Tree control = getWidget();

		control.getColumn(0).setText("foo");
		control.getColumn(1).setText("bar");

		String[] properties1 = new String[] { "firstname", "lastname" };
		String[] headers = new String[] { "First Name", null };
		getRidget().bindToModel(roots, PersonNode.class, "children", "parent", properties1, headers);

		assertEquals("First Name", control.getColumn(0).getText());
		assertEquals("", control.getColumn(1).getText());
	}

	public void testSetGroupingEnabled() {
		TreeTableRidget ridget = getRidget();

		assertFalse(ridget.isGroupingEnabled());

		ridget.setGroupingEnabled(true);

		assertTrue(ridget.isGroupingEnabled());

		ridget.setGroupingEnabled(false);

		assertFalse(ridget.isGroupingEnabled());
	}

	public void testSetGroupingEnabledFiresEvents() {
		TreeTableRidget ridget = getRidget();

		expectPropertyChangeEvent(IGroupedTreeTableRidget.PROPERTY_GROUPING_ENABLED, Boolean.FALSE, Boolean.TRUE);
		ridget.setGroupingEnabled(true);
		verifyPropertyChangeEvents();

		expectNoPropertyChangeEvent();
		ridget.setGroupingEnabled(true);
		verifyPropertyChangeEvents();

		expectPropertyChangeEvent(IGroupedTreeTableRidget.PROPERTY_GROUPING_ENABLED, Boolean.TRUE, Boolean.FALSE);
		ridget.setGroupingEnabled(false);
		verifyPropertyChangeEvents();

		expectNoPropertyChangeEvent();
		ridget.setGroupingEnabled(false);
		verifyPropertyChangeEvents();
	}

	public void testSetComparator() {
		TreeTableRidget ridget = getRidget();
		Tree control = getWidget();

		// sorts from a to z
		Comparator<Object> comparator = new TypedComparator<String>();

		try {
			ridget.setComparator(-1, comparator);
			fail();
		} catch (RuntimeException rex) {
			ok();
		}

		try {
			ridget.setComparator(2, comparator);
			fail();
		} catch (RuntimeException rex) {
			ok();
		}

		ridget.setSortedAscending(true);

		assertEquals("Benjamin", control.getItem(0).getText(0));
		assertEquals("Zebra", control.getItem(1).getText(0));
		assertEquals("Adam", control.getItem(2).getText(0));

		ridget.setComparator(0, comparator);

		assertEquals("Benjamin", control.getItem(0).getText(0));
		assertEquals("Zebra", control.getItem(1).getText(0));
		assertEquals("Adam", control.getItem(2).getText(0));

		ridget.setSortedColumn(0);

		assertEquals("Adam", control.getItem(0).getText(0));
		assertEquals("Benjamin", control.getItem(1).getText(0));
		assertEquals("Zebra", control.getItem(2).getText(0));

		ridget.setComparator(0, null);

		assertEquals("Benjamin", control.getItem(0).getText(0));
		assertEquals("Zebra", control.getItem(1).getText(0));
		assertEquals("Adam", control.getItem(2).getText(0));
	}

	public void testGetSortedColumn() {
		TreeTableRidget ridget = getRidget();

		try {
			ridget.setSortedColumn(2);
			fail();
		} catch (RuntimeException rex) {
			ok();
		}

		assertEquals(-1, ridget.getSortedColumn());

		ridget.setComparator(0, new TypedComparator<String>());

		assertEquals(-1, ridget.getSortedColumn());

		ridget.setSortedColumn(0);

		assertEquals(0, ridget.getSortedColumn());

		ridget.setComparator(0, null);

		assertEquals(-1, ridget.getSortedColumn());

		ridget.setComparator(1, new TypedComparator<String>());
		ridget.setSortedColumn(1);

		assertEquals(1, ridget.getSortedColumn());

		ridget.setSortedColumn(-1);

		assertEquals(-1, ridget.getSortedColumn());

		// no comparator in column 0
		ridget.setSortedColumn(0);

		assertEquals(-1, ridget.getSortedColumn());
	}

	public void testIsColumnSortable() {
		TreeTableRidget ridget = getRidget();

		try {
			assertFalse(ridget.isColumnSortable(-1));
			fail();
		} catch (RuntimeException rex) {
			ok();
		}

		try {
			assertFalse(ridget.isColumnSortable(2));
			fail();
		} catch (RuntimeException rex) {
			ok();
		}

		for (int i = 0; i < 2; i++) {
			assertFalse(ridget.isColumnSortable(i));

			// columns are sortable by default, when they have a comparator
			ridget.setComparator(i, new TypedComparator<String>());

			assertTrue(ridget.isColumnSortable(i));

			ridget.setColumnSortable(i, false);

			assertFalse(ridget.isColumnSortable(i));

			ridget.setColumnSortable(i, true);

			assertTrue(ridget.isColumnSortable(i));

			// columns are not sortable without a comparator
			ridget.setComparator(i, null);

			assertFalse(ridget.isColumnSortable(i));
		}
	}

	public void testSetSortedAscending() {
		Tree control = getWidget();
		TreeTableRidget ridget = getRidget();

		ridget.bindToModel(roots, PersonNode.class, "children", "parent", new String[] { "firstname", "lastname" },
				null);

		assertEquals(-1, ridget.getSortedColumn());
		assertFalse(ridget.isSortedAscending());

		ridget.setComparator(0, new TypedComparator<String>());
		ridget.setSortedColumn(0);

		assertTrue(ridget.isSortedAscending());
		assertEquals("Adam", control.getItem(0).getText(0));
		assertEquals("Benjamin", control.getItem(1).getText(0));
		assertEquals("Zebra", control.getItem(2).getText(0));

		ridget.setSortedAscending(false);

		assertFalse(ridget.isSortedAscending());
		assertEquals("Zebra", control.getItem(0).getText(0));
		assertEquals("Benjamin", control.getItem(1).getText(0));
		assertEquals("Adam", control.getItem(2).getText(0));

		ridget.setSortedAscending(true);

		assertTrue(ridget.isSortedAscending());
		assertEquals("Adam", control.getItem(0).getText(0));
		assertEquals("Benjamin", control.getItem(1).getText(0));
		assertEquals("Zebra", control.getItem(2).getText(0));

		ridget.setComparator(0, null);

		assertEquals(-1, ridget.getSortedColumn());
		assertFalse(ridget.isSortedAscending());
	}

	public void testSetSortedAscendingFiresEvents() {
		TreeTableRidget ridget = getRidget();

		ridget.setSortedAscending(true);

		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, ISortableByColumn.PROPERTY_SORT_ASCENDING,
				Boolean.TRUE, Boolean.FALSE));

		ridget.setSortedAscending(false);

		verifyPropertyChangeEvents();
		expectNoPropertyChangeEvent();

		ridget.setSortedAscending(false);

		verifyPropertyChangeEvents();
		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, ISortableByColumn.PROPERTY_SORT_ASCENDING,
				Boolean.FALSE, Boolean.TRUE));

		ridget.setSortedAscending(true);

		verifyPropertyChangeEvents();
		expectNoPropertyChangeEvent();

		ridget.setSortedAscending(true);

		verifyPropertyChangeEvents();
	}

	public void testSetSortedColumnFiresEvents() {
		TreeTableRidget ridget = getRidget();

		assertEquals(-1, ridget.getSortedColumn());

		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, ISortableByColumn.PROPERTY_SORTED_COLUMN, Integer
				.valueOf(-1), Integer.valueOf(0)));

		ridget.setSortedColumn(0);

		verifyPropertyChangeEvents();
		expectNoPropertyChangeEvent();

		ridget.setSortedColumn(0);

		verifyPropertyChangeEvents();
		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, ISortableByColumn.PROPERTY_SORTED_COLUMN, Integer
				.valueOf(0), Integer.valueOf(1)));

		ridget.setSortedColumn(1);

		verifyPropertyChangeEvents();
	}

	public void testSetColumnSortabilityFiresEvents() {
		TreeTableRidget ridget = getRidget();

		expectNoPropertyChangeEvent();

		ridget.setColumnSortable(0, true);

		verifyPropertyChangeEvents();
		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, ISortableByColumn.PROPERTY_COLUMN_SORTABILITY, null,
				Integer.valueOf(0)));

		ridget.setColumnSortable(0, false);

		verifyPropertyChangeEvents();
		expectNoPropertyChangeEvent();

		ridget.setColumnSortable(0, false);

		verifyPropertyChangeEvents();
		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, ISortableByColumn.PROPERTY_COLUMN_SORTABILITY, null,
				Integer.valueOf(0)));

		ridget.setColumnSortable(0, true);

		verifyPropertyChangeEvents();
		expectNoPropertyChangeEvent();

		ridget.setColumnSortable(0, true);

		verifyPropertyChangeEvents();
	}

	public void testColumnHeaderChangesSortability() {
		TreeTableRidget ridget = getRidget();
		Tree tree = getWidget();

		ridget.setColumnSortable(0, true);
		ridget.setComparator(0, new TypedComparator<String>());
		ridget.setColumnSortable(1, true);
		ridget.setComparator(1, new TypedComparator<String>());

		ridget.setSortedColumn(-1);

		assertEquals(-1, ridget.getSortedColumn());
		assertFalse(ridget.isSortedAscending());

		Event e = new Event();
		e.type = SWT.Selection;
		e.widget = tree.getColumn(0);
		e.widget.notifyListeners(SWT.Selection, e);

		assertEquals(0, ridget.getSortedColumn());
		assertTrue(ridget.isSortedAscending());

		e.widget.notifyListeners(SWT.Selection, e);

		assertEquals(0, ridget.getSortedColumn());
		assertFalse(ridget.isSortedAscending());

		e.widget.notifyListeners(SWT.Selection, e);

		assertEquals(-1, ridget.getSortedColumn());
		assertFalse(ridget.isSortedAscending());

		e.widget.notifyListeners(SWT.Selection, e);

		assertEquals(0, ridget.getSortedColumn());
		assertTrue(ridget.isSortedAscending());
	}

	public void testSortColumnTwo() {
		ITreeTableRidget ridget = getRidget();
		Tree control = getWidget();

		WordNode root = new WordNode("root");
		new WordNode(root, "ZA");
		new WordNode(root, "AAA");
		new WordNode(root, "BCAA");
		ridget.bindToModel(new Object[] { root }, WordNode.class, "children", "parent",
				new String[] { "word", "ACount" }, null);
		ridget.setComparator(0, new TypedComparator<String>());
		ridget.setComparator(1, new TypedComparator<Integer>());
		ridget.expandAll();

		assertEquals("ZA", control.getItem(0).getItem(0).getText());
		assertEquals("AAA", control.getItem(0).getItem(1).getText());
		assertEquals("BCAA", control.getItem(0).getItem(2).getText());

		ridget.setSortedColumn(0);
		ridget.setSortedAscending(true);

		assertEquals("AAA", control.getItem(0).getItem(0).getText());
		assertEquals("BCAA", control.getItem(0).getItem(1).getText());
		assertEquals("ZA", control.getItem(0).getItem(2).getText());
		//TreeUtils.print((Tree) ridget.getUIControl());

		ridget.setSortedColumn(1);

		assertEquals("ZA", control.getItem(0).getItem(0).getText());
		assertEquals("BCAA", control.getItem(0).getItem(1).getText());
		assertEquals("AAA", control.getItem(0).getItem(2).getText());
	}

	/**
	 * Tests that for single selection, the ridget selection state and the ui
	 * selection state cannot be changed by the user when ridget is set to
	 * "output only".
	 */
	public void testOutputSingleSelectionCannotBeChangedFromUI() {
		ITreeTableRidget ridget = getRidget();
		Tree control = getWidget();

		ridget.setSelectionType(SelectionType.SINGLE);

		assertEquals(0, ridget.getSelection().size());
		assertEquals(0, control.getSelectionCount());

		ridget.setOutputOnly(true);
		control.setFocus();
		// move down and up to select row 0; space does not select in tables
		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_DOWN);
		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_UP);

		assertEquals(0, ridget.getSelection().size());
		assertEquals(0, control.getSelectionCount());

		ridget.setOutputOnly(false);
		control.setFocus();
		// move down and up to select row 0; space does not select in tables
		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_DOWN);
		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_UP);

		assertEquals(1, ridget.getSelection().size());
		assertEquals(1, control.getSelectionCount());
	}

	/**
	 * Tests that for multiple selection, the ridget selection state and the ui
	 * selection state cannot be changed by the user when ridget is set to
	 * "output only".
	 */
	public void testOutputMultipleSelectionCannotBeChangedFromUI() {
		ITreeTableRidget ridget = getRidget();
		Tree control = getWidget();

		ridget.setSelectionType(SelectionType.MULTI);

		assertEquals(0, ridget.getSelection().size());
		assertEquals(0, control.getSelectionCount());

		ridget.setOutputOnly(true);
		control.setFocus();
		// move down and up to select row 0; space does not select in tables
		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_DOWN);
		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_UP);

		assertEquals(0, ridget.getSelection().size());
		assertEquals(0, control.getSelectionCount());

		ridget.setOutputOnly(false);
		control.setFocus();
		// move down and up to select row 0; space does not select in tables
		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_DOWN);
		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_UP);

		assertEquals(1, ridget.getSelection().size());
		assertEquals(1, control.getSelectionCount());
	}

	/**
	 * Tests that toggling output state on/off does not change the selection.
	 */
	public void testTogglingOutputDoesNotChangeSelection() {
		ITreeTableRidget ridget = getRidget();

		ridget.setSelection(node1);

		assertEquals(1, ridget.getSelection().size());

		ridget.setOutputOnly(true);

		assertEquals(1, ridget.getSelection().size());

		ridget.setSelection((Object) null);

		assertEquals(0, ridget.getSelection().size());

		ridget.setOutputOnly(false);

		assertEquals(0, ridget.getSelection().size());
	}

	public void testSetColumnFormatter() {
		ITreeTableRidget ridget = getRidget();
		Tree tree = getWidget();
		IColumnFormatter formatter = new ColumnFormatter() {
			@Override
			public String getText(Object element) {
				PersonNode node = (PersonNode) element;
				return node.getLastname().toUpperCase();
			}
		};
		final String lastName = node1.getLastname();
		final String LASTNAME = lastName.toUpperCase();

		try {
			ridget.setColumnFormatter(-1, formatter);
			fail();
		} catch (RuntimeException rex) {
			ok();
		}

		try {
			ridget.setColumnFormatter(99, formatter);
			fail();
		} catch (RuntimeException rex) {
			ok();
		}

		ridget.setColumnFormatter(1, formatter);

		assertEquals(lastName, tree.getItem(0).getText(1));

		ridget.updateFromModel();

		assertEquals(LASTNAME, tree.getItem(0).getText(1));

		ridget.setColumnFormatter(1, null);

		assertEquals(LASTNAME, tree.getItem(0).getText(1));

		ridget.updateFromModel();

		assertEquals(lastName, tree.getItem(0).getText(1));
	}

	// helping methods
	// ////////////////

	private PersonNode[] initializeTreeModel() {
		// root nodes
		PersonNode root1 = new PersonNode(new Person("Root", "Benjamin"));
		node1 = root1;
		PersonNode root2 = new PersonNode(new Person("Root", "Zebra"));
		PersonNode root3 = new PersonNode(new Person("Root", "Adam"));
		// children beaneath node1
		node2 = new PersonNode(node1, new Person("Benjason", "Elen"));
		node4 = new PersonNode(node1, new Person("Benjason", "Zora"));
		new PersonNode(node1, new Person("Benjason", "Anna"));
		// children beneath node2
		node3 = new PersonNode(node2, new Person("Zebrason", "Ben"));
		new PersonNode(node2, new Person("Zebrason", "Zulu"));
		new PersonNode(node2, new Person("Zebrason", "Arno"));

		return new PersonNode[] { root1, root2, root3 };
	}

	/**
	 * Return the TreeItem corresponding to the following mock "index" scheme:
	 * 0: item for node1, 1: item for node2, 2: item for node3, 3: item for
	 * node4.
	 * <p>
	 * This method will fully expand the tree to ensure all tree items are
	 * created.
	 */
	private TreeItem getUIControlItem(int index) {
		getRidget().expandAll();
		Tree control = getWidget();
		switch (index) {
		case 0:
			return control.getItem(0);
		case 1:
			return control.getItem(0).getItem(0);
		case 2:
			return control.getItem(0).getItem(0).getItem(0);
		case 3:
			return control.getItem(0).getItem(1);
		default:
			throw new IndexOutOfBoundsException("index= " + index);
		}
	}

	// helping classes
	// ////////////////

	/**
	 * Wraps a {@link Person} to make it compatible with the
	 * {@link ITreeTableRidget}.
	 */
	private static final class PersonNode extends TreeNode {

		public PersonNode(Person value) {
			super(value);
		}

		public PersonNode(PersonNode parent, Person value) {
			super(parent, value);
		}

		public String getFirstname() {
			return ((Person) getValue()).getFirstname();
		}

		public String getLastname() {
			return ((Person) getValue()).getLastname();
		}

		public String getEntry() {
			// 'Last Name' - 'First Name'
			return ((Person) getValue()).getListEntry();
		}

	}

}
