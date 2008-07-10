/*******************************************************************************
 * Copyright (c) 2007 compeople AG and others.
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
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Date;

import org.easymock.EasyMock;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.riena.core.marker.IMarker;
import org.eclipse.riena.navigation.ui.swt.binding.DefaultSwtControlRidgetMapper;
import org.eclipse.riena.tests.UITestHelper;
import org.eclipse.riena.ui.core.marker.ErrorMarker;
import org.eclipse.riena.ui.core.marker.IMessageMarker;
import org.eclipse.riena.ui.ridgets.IRidget;
import org.eclipse.riena.ui.ridgets.ITextFieldRidget;
import org.eclipse.riena.ui.ridgets.databinding.DateToStringConverter;
import org.eclipse.riena.ui.ridgets.databinding.StringToDateConverter;
import org.eclipse.riena.ui.ridgets.util.beans.DateBean;
import org.eclipse.riena.ui.ridgets.util.beans.StringBean;
import org.eclipse.riena.ui.ridgets.util.beans.TestBean;
import org.eclipse.riena.ui.ridgets.validation.MaxLength;
import org.eclipse.riena.ui.ridgets.validation.MinLength;
import org.eclipse.riena.ui.ridgets.validation.ValidCharacters;
import org.eclipse.riena.ui.ridgets.validation.ValidEmailAddress;
import org.eclipse.riena.ui.ridgets.validation.ValidIntermediateDate;
import org.eclipse.riena.ui.ridgets.validation.ValidationFailure;
import org.eclipse.riena.ui.ridgets.validation.ValidationRuleStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Tests of the class <code>TextFieldRidget</code>.
 */
public class TextRidgetTest2 extends AbstractSWTRidgetTest {

	private final static String TEXT_ONE = "TestText1";
	private final static String TEXT_TWO = "TestText2";
	private TestBean bean;
	private PropertyChangeListener propertyChangeListenerMock;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		bean = new TestBean();
		propertyChangeListenerMock = EasyMock.createMock(PropertyChangeListener.class);
		getRidget().addPropertyChangeListener(propertyChangeListenerMock);
		Shell shell = getShell();
		// need a second control, so that we can switch focus
		new Text(shell, SWT.BORDER);
		shell.layout();
	}

	@Override
	protected IRidget createRidget() {
		return new TextRidget();
	}

	@Override
	protected Control createUIControl(Composite parent) {
		return new Text(getShell(), SWT.BORDER);
	}

	@Override
	protected ITextFieldRidget getRidget() {
		return (ITextFieldRidget) super.getRidget();
	}

	@Override
	protected Text getUIControl() {
		return (Text) super.getUIControl();
	}

	public void testRidgetMapping() {
		DefaultSwtControlRidgetMapper mapper = new DefaultSwtControlRidgetMapper();
		assertSame(TextRidget.class, mapper.getRidgetClass(getUIControl()));
	}

	public void testInitialValueFromModel() {
		Text control = getUIControl();
		ITextFieldRidget ridget = getRidget();

		bean.setProperty(TEXT_TWO);

		ridget.bindToModel(bean, TestBean.PROPERTY);

		assertEquals("", control.getText());
		assertEquals("", ridget.getText());
		assertEquals(TEXT_TWO, bean.getProperty());

		expectPropertyChangeEvent(ITextFieldRidget.PROPERTY_TEXT, "", TEXT_TWO);

		ridget.updateFromModel();

		verifyPropertyChangeEvents();
		assertEquals(TEXT_TWO, control.getText());
		assertEquals(TEXT_TWO, ridget.getText());
		assertEquals(TEXT_TWO, bean.getProperty());
	}

	public void testUpdateFromModel() {
		Text control = getUIControl();
		ITextFieldRidget ridget = getRidget();

		ridget.bindToModel(bean, TestBean.PROPERTY);

		assertEquals("", control.getText());
		assertEquals("", ridget.getText());
		assertEquals(null, bean.getProperty());

		bean.setProperty(TEXT_ONE);
		ridget.updateFromModel();

		assertEquals(TEXT_ONE, control.getText());
		assertEquals(TEXT_ONE, ridget.getText());
		assertEquals(TEXT_ONE, bean.getProperty());

		expectNoPropertyChangeEvent();

		bean.setProperty(TEXT_TWO);

		verifyPropertyChangeEvents();
		assertEquals(TEXT_ONE, control.getText());
		assertEquals(TEXT_ONE, ridget.getText());
		assertEquals(TEXT_TWO, bean.getProperty());

		expectPropertyChangeEvent(ITextFieldRidget.PROPERTY_TEXT, TEXT_ONE, TEXT_TWO);

		ridget.updateFromModel();

		verifyPropertyChangeEvents();
		assertEquals(TEXT_TWO, control.getText());
		assertEquals(TEXT_TWO, ridget.getText());
		assertEquals(TEXT_TWO, bean.getProperty());
	}

	public void testUpdateFromModelDirectWriting() {
		Text control = getUIControl();
		ITextFieldRidget ridget = getRidget();

		ridget.setDirectWriting(true);

		ridget.bindToModel(bean, TestBean.PROPERTY);

		bean.setProperty(TEXT_ONE);
		ridget.updateFromModel();

		assertEquals(TEXT_ONE, control.getText());
		assertEquals(TEXT_ONE, ridget.getText());
		assertEquals(TEXT_ONE, bean.getProperty());

		bean.setProperty(TEXT_TWO);
		ridget.updateFromModel();

		assertEquals(TEXT_TWO, control.getText());
		assertEquals(TEXT_TWO, ridget.getText());
		assertEquals(TEXT_TWO, bean.getProperty());
	}

	public void testUpdateFromControlUserInput() throws Exception {
		Text control = getUIControl();
		ITextFieldRidget ridget = getRidget();
		Display display = control.getDisplay();

		ridget.bindToModel(bean, TestBean.PROPERTY);

		UITestHelper.sendString(display, "test");

		assertEquals("test", control.getText());
		assertEquals("", ridget.getText());
		assertEquals(null, bean.getProperty());

		expectPropertyChangeEvent(ITextFieldRidget.PROPERTY_TEXT, "", "test");

		UITestHelper.sendString(display, "\r");
		UITestHelper.readAndDispatch(control);

		verifyPropertyChangeEvents();
		assertEquals("test", control.getText());
		assertEquals("test", ridget.getText());
		assertEquals("test", bean.getProperty());

		expectNoPropertyChangeEvent();

		UITestHelper.sendString(display, "2");

		verifyPropertyChangeEvents();
		assertEquals("test2", control.getText());
		assertEquals("test", ridget.getText());
		assertEquals("test", bean.getProperty());

		expectPropertyChangeEvent(ITextFieldRidget.PROPERTY_TEXT, "test", "test2");

		UITestHelper.sendString(display, "\t");

		verifyPropertyChangeEvents();
		assertEquals("test2", control.getText());
		assertEquals("test2", ridget.getText());
		assertEquals("test2", bean.getProperty());
	}

	public void testUpdateFromControlUserInputDirectWriting() {
		Text control = getUIControl();
		ITextFieldRidget ridget = getRidget();

		ridget.bindToModel(bean, TestBean.PROPERTY);
		ridget.setDirectWriting(true);

		Display display = control.getDisplay();
		UITestHelper.sendString(display, "t");

		assertEquals("t", control.getText());
		assertEquals("t", ridget.getText());
		assertEquals("t", bean.getProperty());

		expectPropertyChangeEvent(ITextFieldRidget.PROPERTY_TEXT, "t", "te");

		UITestHelper.sendString(display, "e");

		verifyPropertyChangeEvents();
		assertEquals("te", control.getText());
		assertEquals("te", ridget.getText());
		assertEquals("te", bean.getProperty());

		expectPropertyChangeEvent(ITextFieldRidget.PROPERTY_TEXT, "te", "tes");

		UITestHelper.sendString(display, "s");

		verifyPropertyChangeEvents();
		assertEquals("tes", control.getText());
		assertEquals("tes", ridget.getText());
		assertEquals("tes", bean.getProperty());

		expectPropertyChangeEvent(ITextFieldRidget.PROPERTY_TEXT, "tes", "test");

		UITestHelper.sendString(display, "t");

		verifyPropertyChangeEvents();
		assertEquals("test", control.getText());
		assertEquals("test", ridget.getText());
		assertEquals("test", bean.getProperty());

		expectPropertyChangeEvent(ITextFieldRidget.PROPERTY_TEXT, "test", "tet");

		UITestHelper.sendKeyAction(display, SWT.ARROW_LEFT);
		UITestHelper.sendString(display, "\b");

		verifyPropertyChangeEvents();
		assertEquals("tet", control.getText());
		assertEquals("tet", ridget.getText());
		assertEquals("tet", bean.getProperty());

		expectPropertyChangeEvent(ITextFieldRidget.PROPERTY_TEXT, "tet", "te");

		UITestHelper.sendString(display, String.valueOf(SWT.DEL));

		verifyPropertyChangeEvents();
		assertEquals("te", control.getText());
		assertEquals("te", ridget.getText());
		assertEquals("te", bean.getProperty());

		expectNoPropertyChangeEvent();

		bean.setProperty("Test");

		verifyPropertyChangeEvents();
		assertEquals("te", control.getText());
		assertEquals("te", ridget.getText());
		assertEquals("Test", bean.getProperty());

		expectPropertyChangeEvent(ITextFieldRidget.PROPERTY_TEXT, "te", "t");

		UITestHelper.sendString(display, "\b");

		verifyPropertyChangeEvents();
		assertEquals("t", control.getText());
		assertEquals("t", ridget.getText());
		assertEquals("t", bean.getProperty());
	}

	public void testUpdateFromControlMethodCall() throws Exception {
		Text control = getUIControl();
		ITextFieldRidget ridget = getRidget();

		ridget.bindToModel(bean, TestBean.PROPERTY);

		((Text) ridget.getUIControl()).setText("test");

		assertEquals("test", control.getText());
		assertEquals("", ridget.getText());
		assertEquals(null, bean.getProperty());

		expectPropertyChangeEvent(ITextFieldRidget.PROPERTY_TEXT, "", "test");

		UITestHelper.sendString(control.getDisplay(), "\r");

		verifyPropertyChangeEvents();
		assertEquals("test", control.getText());
		assertEquals("test", ridget.getText());
		assertEquals("test", bean.getProperty());

		expectNoPropertyChangeEvent();

		((Text) ridget.getUIControl()).setText("TEST2");

		verifyPropertyChangeEvents();
		assertEquals("TEST2", control.getText());
		assertEquals("test", ridget.getText());
		assertEquals("test", bean.getProperty());

		expectPropertyChangeEvent(ITextFieldRidget.PROPERTY_TEXT, "test", "TEST2");

		UITestHelper.sendString(control.getDisplay(), "\t");

		verifyPropertyChangeEvents();
		assertEquals("TEST2", control.getText());
		assertEquals("TEST2", ridget.getText());
		assertEquals("TEST2", bean.getProperty());
	}

	public void testUpdateFromControlMethodCallDirectWriting() {
		Text control = getUIControl();
		ITextFieldRidget ridget = getRidget();

		ridget.bindToModel(bean, TestBean.PROPERTY);

		ridget.setDirectWriting(true);

		((Text) ridget.getUIControl()).setText("t");

		assertEquals("t", control.getText());
		assertEquals("t", ridget.getText());
		assertEquals("t", bean.getProperty());

		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, ITextFieldRidget.PROPERTY_TEXT, "t", "Test"));

		((Text) ridget.getUIControl()).setText("Test");

		verifyPropertyChangeEvents();
		assertEquals("Test", control.getText());
		assertEquals("Test", ridget.getText());
		assertEquals("Test", bean.getProperty());

		expectNoPropertyChangeEvent();

		bean.setProperty("Toast");

		verifyPropertyChangeEvents();
		assertEquals("Test", control.getText());
		assertEquals("Test", ridget.getText());
		assertEquals("Toast", bean.getProperty());

		expectPropertyChangeEvent(ITextFieldRidget.PROPERTY_TEXT, "Test", "Test2");

		UITestHelper.sendKeyAction(control.getDisplay(), SWT.END);
		UITestHelper.sendString(control.getDisplay(), "2");

		verifyPropertyChangeEvents();
		assertEquals("Test2", control.getText());
		assertEquals("Test2", ridget.getText());
		assertEquals("Test2", bean.getProperty());
	}

	public void testUpdateFromRidget() throws Exception {
		Text control = getUIControl();
		ITextFieldRidget ridget = getRidget();

		ridget.bindToModel(bean, TestBean.PROPERTY);

		ridget.setText(TEXT_TWO);

		assertEquals(TEXT_TWO, control.getText());
		assertEquals(TEXT_TWO, ridget.getText());
		assertEquals(TEXT_TWO, bean.getProperty());

		expectNoPropertyChangeEvent();

		control.selectAll();
		UITestHelper.sendString(control.getDisplay(), "12");
		bean.setProperty("Bean34");

		verifyPropertyChangeEvents();
		assertEquals("12", control.getText());
		assertEquals(TEXT_TWO, ridget.getText());
		assertEquals("Bean34", bean.getProperty());

		expectPropertyChangeEvent(ITextFieldRidget.PROPERTY_TEXT, TEXT_TWO, TEXT_ONE);

		ridget.setText(TEXT_ONE);

		verifyPropertyChangeEvents();
		assertEquals(TEXT_ONE, control.getText());
		assertEquals(TEXT_ONE, ridget.getText());
		assertEquals(TEXT_ONE, bean.getProperty());
	}

	public void testUpdateFromRidgetOnRebind() throws Exception {
		Text control = getUIControl();
		ITextFieldRidget ridget = getRidget();

		ridget.bindToModel(bean, TestBean.PROPERTY);

		bean.setProperty(TEXT_ONE);
		ridget.updateFromModel();

		assertEquals(TEXT_ONE, control.getText());
		assertEquals(TEXT_ONE, ridget.getText());
		assertEquals(TEXT_ONE, bean.getProperty());

		// unbind, e.g. when the view is used by another controller
		ridget.setUIControl(null);

		control.selectAll();
		UITestHelper.sendString(control.getDisplay(), "xy");

		assertEquals("xy", control.getText());
		assertEquals(TEXT_ONE, ridget.getText());
		assertEquals(TEXT_ONE, bean.getProperty());

		// rebind
		ridget.setUIControl(control);

		assertEquals(TEXT_ONE, control.getText());
		assertEquals(TEXT_ONE, ridget.getText());
		assertEquals(TEXT_ONE, bean.getProperty());

		// unbind again
		ridget.setUIControl(null);

		bean.setProperty(TEXT_TWO);
		ridget.updateFromModel();

		assertEquals(TEXT_ONE, control.getText());
		assertEquals(TEXT_TWO, ridget.getText());
		assertEquals(TEXT_TWO, bean.getProperty());

		// rebind
		ridget.setUIControl(control);

		assertEquals(TEXT_TWO, control.getText());
		assertEquals(TEXT_TWO, ridget.getText());
		assertEquals(TEXT_TWO, bean.getProperty());
	}

	public void testCaretPositionAfterBind() throws Exception {
		Text control = getUIControl();
		ITextFieldRidget ridget = getRidget();

		ridget.bindToModel(bean, TestBean.PROPERTY);

		bean.setProperty(TEXT_ONE);
		ridget.updateFromModel();

		assertEquals(TEXT_ONE, control.getText());
		assertEquals(0, control.getCaretPosition());

		UITestHelper.sendKeyAction(control.getDisplay(), SWT.RIGHT);
		UITestHelper.sendKeyAction(control.getDisplay(), SWT.RIGHT);
		bean.setProperty(TEXT_TWO);
		ridget.updateFromModel();

		assertEquals(0, control.getCaretPosition());

		Text text = new Text(getShell(), SWT.MULTI);
		text.setText("foo");
		text.setSelection(2, 2);

		ridget.setUIControl(text);

		assertEquals(TEXT_TWO, text.getText());
		assertEquals(0, text.getCaretPosition());
	}

	public void testValidationOnUpdateToModel() throws Exception {
		Text control = getUIControl();
		ITextFieldRidget ridget = getRidget();

		ridget.bindToModel(bean, TestBean.PROPERTY);

		ridget.addValidationRule(new MinLength(3), false);

		bean.setProperty(TEXT_ONE);
		ridget.updateFromModel();

		assertTrue(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
		assertEquals(TEXT_ONE, ridget.getText());

		// ridget.setText("xy");
		control.selectAll();
		UITestHelper.sendString(control.getDisplay(), "xy\t");

		assertFalse(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
		assertEquals("xy", ridget.getText());

		control.setFocus();
		UITestHelper.sendKeyAction(control.getDisplay(), SWT.END);
		UITestHelper.sendString(control.getDisplay(), "z");

		assertFalse(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());

		UITestHelper.sendString(control.getDisplay(), "\r");

		assertTrue(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
		assertEquals("xyz", ridget.getText());
	}

	public void testValidationOnKeyPressedWithBlocking() throws Exception {
		Text control = getUIControl();
		ITextFieldRidget ridget = getRidget();

		ridget.bindToModel(bean, TestBean.PROPERTY);

		ridget.addValidationRule(new ValidCharacters(ValidCharacters.VALID_NUMBERS), true);
		ridget.setDirectWriting(true);

		UITestHelper.sendString(control.getDisplay(), "12");

		assertEquals("12", control.getText());
		assertEquals("12", ridget.getText());
		assertEquals("12", bean.getProperty());

		UITestHelper.sendString(control.getDisplay(), "d");

		assertEquals("12", control.getText());
		assertEquals("12", ridget.getText());
		assertEquals("12", bean.getProperty());
	}

	public void testValidationOnKeyPressedWithoutBlocking() throws Exception {
		Text control = getUIControl();
		ITextFieldRidget ridget = getRidget();

		DateBean dateBean = new DateBean();
		dateBean.setValue(new Date(0));

		fail("TODO - implement segmentend date text field");
		// TODO control = UIControlsFactory.createSegmentedDateTextField();

		ridget.addValidationRule(new ValidIntermediateDate("dd.MM.yyyy"), true);
		ridget.setUIControlToModelConverter(new StringToDateConverter("dd.MM.yyyy"));
		ridget.setModelToUIControlConverter(new DateToStringConverter("dd.MM.yyyy"));

		ridget.setUIControl(control);
		ridget.bindToModel(dateBean, DateBean.DATE_PROPERTY);
		ridget.updateFromModel();

		assertEquals("01.01.1970", control.getText());
		assertEquals("01.01.1970", ridget.getText());
		assertTrue(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());

		UITestHelper.sendKeyAction(control.getDisplay(), SWT.DEL);
		UITestHelper.sendString(control.getDisplay(), "4");
		ridget.addMarker(new ErrorMarker());

		assertEquals("41.01.1970", control.getText());
		assertEquals("01.01.1970", ridget.getText());
		assertFalse(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
	}

	public void testValidationOnSetTextWithOnEditRule() {
		ITextFieldRidget ridget = getRidget();
		StringBean model = new StringBean();
		ridget.bindToModel(model, StringBean.PROP_VALUE);

		IValidator onEditRule = new MaxLength(5);

		assertFalse(ridget.isErrorMarked());

		ridget.setText("tiny");
		ridget.addValidationRule(onEditRule, true);

		try {
			ridget.setText("too long");
			fail();
		} catch (RuntimeException rex) {
			// expected
		}

		assertFalse(ridget.isErrorMarked());
		assertEquals("tiny", ridget.getText());
		assertEquals("tiny", getUIControl().getText());
		assertEquals("tiny", model.getValue());

		ridget.setText("short");

		assertFalse(ridget.isErrorMarked());
		assertEquals("short", ridget.getText());
		assertEquals("short", getUIControl().getText());
		assertEquals("short", model.getValue());
	}

	public void testValidationOnSetTextWithOnUpdateRule() {
		ITextFieldRidget ridget = getRidget();
		StringBean model = new StringBean();
		ridget.bindToModel(model, StringBean.PROP_VALUE);

		IValidator onUpdateRule = new MinLength(10);

		assertFalse(ridget.isErrorMarked());

		ridget.setText("this is long enough");
		ridget.addValidationRule(onUpdateRule, false);

		try {
			ridget.setText("tiny");
			fail();
		} catch (RuntimeException rex) {
			// expected
		}

		assertFalse(ridget.isErrorMarked());
		assertEquals("this is long enough", ridget.getText());
		assertEquals("this is long enough", getUIControl().getText());
		assertEquals("this is long enough", model.getValue());

		ridget.setText("this is not too short");

		assertFalse(ridget.isErrorMarked());
		assertEquals("this is not too short", ridget.getText());
		assertEquals("this is not too short", getUIControl().getText());
		assertEquals("this is not too short", model.getValue());
	}

	public void testValidationOnUpdateFromModelWithOnEditRule() {
		ITextFieldRidget ridget = getRidget();
		StringBean model = new StringBean();
		ridget.bindToModel(model, StringBean.PROP_VALUE);

		IValidator onEditRule = new MaxLength(5);

		assertFalse(ridget.isErrorMarked());

		ridget.setText("tiny");
		ridget.addValidationRule(onEditRule, true);
		model.setValue("too long");

		try {
			ridget.updateFromModel();
			fail();
		} catch (RuntimeException rex) {
			// expected
		}

		assertFalse(ridget.isErrorMarked());
		assertEquals("tiny", ridget.getText());
		assertEquals("tiny", getUIControl().getText());
		assertEquals("too long", model.getValue());

		model.setValue("short");
		ridget.updateFromModel();

		assertFalse(ridget.isErrorMarked());
		assertEquals("short", ridget.getText());
		assertEquals("short", getUIControl().getText());
		assertEquals("short", model.getValue());
	}

	public void testValidationOnUpdateFromModelWithOnUpdateRule() {
		ITextFieldRidget ridget = getRidget();
		StringBean model = new StringBean();
		ridget.bindToModel(model, StringBean.PROP_VALUE);

		IValidator onUpdateRule = new MinLength(10);

		assertFalse(ridget.isErrorMarked());

		ridget.setText("something long");
		ridget.addValidationRule(onUpdateRule, false);
		model.setValue("tiny");

		try {
			ridget.updateFromModel();
			fail();
		} catch (RuntimeException rex) {
			// expected
		}

		assertFalse(ridget.isErrorMarked());
		assertEquals("something long", ridget.getText());
		assertEquals("something long", getUIControl().getText());
		assertEquals("tiny", model.getValue());

		model.setValue("this is not too short");
		ridget.updateFromModel();

		assertFalse(ridget.isErrorMarked());
		assertEquals("this is not too short", ridget.getText());
		assertEquals("this is not too short", getUIControl().getText());
		assertEquals("this is not too short", model.getValue());
	}

	public void testUpdateFromRidgetWithValidationOnEditRule() {
		Text control = getUIControl();
		ITextFieldRidget ridget = getRidget();

		ridget.addValidationRule(new ValidEmailAddress(), true);
		ridget.bindToModel(bean, TestBean.PROPERTY);

		assertFalse(ridget.isErrorMarked());
		assertFalse(ridget.isDirectWriting());

		// a little workaround because UITestHelper cannot send '@'
		control.setText("a@");
		control.setSelection(3, 3);
		// \t triggers update
		UITestHelper.sendString(control.getDisplay(), "b.com\t");

		assertFalse(ridget.isErrorMarked());
		assertEquals("a@b.com", ridget.getText());
		assertEquals("a@b.com", bean.getProperty());

		control.setFocus();
		control.selectAll();
		// \t triggers update
		UITestHelper.sendString(control.getDisplay(), "invalid\t");

		assertTrue(ridget.isErrorMarked());
		assertEquals("a@b.com", ridget.getText());
		assertEquals("a@b.com", bean.getProperty());

		// a little workaround because UITestHelper cannot send '@'
		control.setText("c@");
		control.setFocus();
		control.setSelection(3, 3);
		// \t triggers update
		UITestHelper.sendString(control.getDisplay(), "d.com\t");

		assertFalse(ridget.isErrorMarked());
		assertEquals("c@d.com", ridget.getText());
		assertEquals("c@d.com", bean.getProperty());
	}

	public void testUpdateFromRidgetWithValidationOnUpdateRule() {
		Text control = getUIControl();
		ITextFieldRidget ridget = getRidget();

		ridget.addValidationRule(new ValidEmailAddress(), false);
		ridget.bindToModel(bean, TestBean.PROPERTY);

		assertFalse(ridget.isErrorMarked());
		assertFalse(ridget.isDirectWriting());

		// a little workaround because UITestHelper cannot send '@'
		control.setText("a@");
		control.setSelection(3, 3);
		// \t triggers update
		UITestHelper.sendString(control.getDisplay(), "b.com\t");

		assertFalse(ridget.isErrorMarked());
		assertEquals("a@b.com", ridget.getText());
		assertEquals("a@b.com", bean.getProperty());

		control.setFocus();
		control.selectAll();
		// \t triggers update
		UITestHelper.sendString(control.getDisplay(), "invalid\t");

		assertTrue(ridget.isErrorMarked());
		assertEquals("invalid", ridget.getText());
		assertEquals("a@b.com", bean.getProperty());

		// a little workaround because UITestHelper cannot send '@'
		control.setText("c@");
		control.setFocus();
		control.setSelection(3, 3);
		// \t triggers update
		UITestHelper.sendString(control.getDisplay(), "d.com\t");

		assertFalse(ridget.isErrorMarked());
		assertEquals("c@d.com", ridget.getText());
		assertEquals("c@d.com", bean.getProperty());
	}

	public void testValidationMessageWithOnEditRule() throws Exception {
		Text control = getUIControl();
		ITextFieldRidget ridget = getRidget();

		ridget.bindToModel(bean, TestBean.PROPERTY);
		ridget.addValidationRule(new EvenNumberOfCharacters(), true);
		ridget.setDirectWriting(true);

		ridget.addValidationMessage("TestTextTooShortMessage");

		assertEquals(0, ridget.getMarkers().size());

		UITestHelper.sendString(control.getDisplay(), "a");

		assertEquals(2, ridget.getMarkers().size());
		assertEquals("TestTextTooShortMessage", getMessageMarker(ridget.getMarkers()).getMessage());

		UITestHelper.sendString(control.getDisplay(), "b");

		assertEquals(0, ridget.getMarkers().size());
	}

	public void testValidationMessageWithOnUpdateRule() throws Exception {
		Text control = getUIControl();
		ITextFieldRidget ridget = getRidget();

		ridget.bindToModel(bean, TestBean.PROPERTY);
		ridget.addValidationRule(new EvenNumberOfCharacters(), false);
		ridget.setDirectWriting(true);

		ridget.addValidationMessage("TestTextTooShortMessage");

		assertEquals(0, ridget.getMarkers().size());

		// \r triggers update
		UITestHelper.sendString(control.getDisplay(), "a\r");

		assertEquals(2, ridget.getMarkers().size());
		assertEquals("TestTextTooShortMessage", getMessageMarker(ridget.getMarkers()).getMessage());

		// \r triggers update
		UITestHelper.sendString(control.getDisplay(), "b\r");

		assertEquals(0, ridget.getMarkers().size());
	}

	// helping methods
	// ////////////////

	private IMessageMarker getMessageMarker(Collection<? extends IMarker> markers) {
		for (IMarker marker : markers) {
			if (marker instanceof IMessageMarker) {
				return (IMessageMarker) marker;
			}
		}
		return null;
	}

	private static class EvenNumberOfCharacters implements IValidator {

		public IStatus validate(final Object value) {
			if (value == null) {
				return ValidationRuleStatus.ok();
			}
			if (value instanceof String) {
				final String string = (String) value;
				if (string.length() % 2 == 0) {
					return ValidationRuleStatus.ok();
				}
				return ValidationRuleStatus.error(false, "Odd number of characters.", this);
			}
			throw new ValidationFailure(getClass().getName() + " can only validate objects of type "
					+ String.class.getName());
		}

	}

}
