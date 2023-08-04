ruleset {
    description 'Grails-CodeNarc Project RuleSet'

    ruleset('rulesets/basic.xml')
    ruleset('rulesets/braces.xml')
    ruleset('rulesets/convention.xml'){
        'CompileStatic' {
            enabled = false
        }
        'ImplicitReturnStatement' {
            enabled = false
        }
        'NoDef' {
            enabled = false
        }
        'VariableTypeRequired' {
            enabled = false
        }
        'ImplicitClosureParameter' {
            enabled = false
        }
    }
    ruleset('rulesets/design.xml')
    ruleset('rulesets/dry.xml')
    ruleset('rulesets/exceptions.xml')
    ruleset('rulesets/formatting.xml'){
        'ClassEndsWithBlankLine' {
            enabled = false
        }
        'ClassStartsWithBlankLine' {
            enabled = false
        }
        'FileEndsWithoutNewline' {
            enabled = false
        }
        'LineLength' {
            length = 150
        }
        'SpaceAroundMapEntryColon' {
            enabled = false
        }
        'SpaceBeforeOpeningBrace' {
            enabled = false
        }
        'SpaceBeforeClosingBrace' {
            enabled = false
        }
        'SpaceAfterOpeningBrace' {
            enabled = false
        }
    }
    ruleset('rulesets/generic.xml')
    ruleset('rulesets/imports.xml'){
        'MisorderedStaticImports' {
            enabled = false
        }
    }
    ruleset('rulesets/naming.xml'){
        'MethodName' {
            enabled = false
        }
    }
    ruleset('rulesets/unnecessary.xml')
    ruleset('rulesets/unused.xml')
}