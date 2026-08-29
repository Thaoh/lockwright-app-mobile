import { render } from '@testing-library/react-native'

import { LogoLock } from './index'

describe('LogoLock Component', () => {
  it('renders correctly with given width and height', () => {
    const { toJSON } = render(<LogoLock width="100" height="150" />)
    const tree = toJSON()

    expect(tree.props.width).toBe('100')
    expect(tree.props.height).toBe('150')
    expect(tree).toMatchSnapshot()
  })

  it('renders correctly without width and height', () => {
    const { toJSON } = render(<LogoLock />)
    const tree = toJSON()

    expect(tree.props.width).toBeUndefined()
    expect(tree.props.height).toBeUndefined()

    expect(tree).toMatchSnapshot()
  })

  it('uses the hatch-plate brass, not PearPass lime', () => {
    const { toJSON } = render(<LogoLock />)
    const src = JSON.stringify(toJSON())
    expect(src).toContain('4289760599')
    expect(src).toContain('"vbWidth":64')
    expect(src).not.toMatch(/#B0D944|#BADE5B/i)
  })
})
