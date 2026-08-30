import { render } from '@testing-library/react-native'

import { PasswordAcceptChecklist } from './PasswordAcceptChecklist'

const labels = {
  minLength: 'At least 8 characters',
  hasLowerCase: 'One lowercase letter',
  hasUpperCase: 'One uppercase letter',
  hasNumbers: 'One number',
  hasSymbols: 'One special character'
}

describe('PasswordAcceptChecklist', () => {
  it('marks met rules checked and unmet rules unchecked', () => {
    const { getByTestId, getByText } = render(
      <PasswordAcceptChecklist
        ticks={{
          minLength: true,
          hasLowerCase: true,
          hasUpperCase: true,
          hasNumbers: false,
          hasSymbols: false
        }}
        labels={labels}
      />
    )

    expect(getByText('At least 8 characters')).toBeTruthy()
    expect(
      getByTestId('password-accept-rule-minLength').props.accessibilityState
    ).toEqual({ checked: true })
    expect(
      getByTestId('password-accept-rule-hasNumbers').props.accessibilityState
    ).toEqual({ checked: false })
    expect(
      getByTestId('password-accept-rule-hasSymbols').props.accessibilityState
    ).toEqual({ checked: false })
  })
})
